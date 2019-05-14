package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.IFilterService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskAuthenticationService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IFilterService filterService;

    @Autowired
    private IDataService dataService;

    @Autowired
	private IUserService userService;

    @Autowired
    private ITaskAuthenticationService taskAuthenticationService;

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable, locale);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, com.netgrif.workflow.workflow.domain.Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public PagedResources<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, com.netgrif.workflow.workflow.domain.Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/{id}", method = RequestMethod.GET)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return taskService.findAllByCase(caseId, locale);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        Task task = taskService.findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new LocalisedTask(task, locale));
    }

    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET)
    public MessageResource assign(Authentication auth, @PathVariable("id") String taskId) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if( !loggedUser.isAdmin() && !taskAuthenticationService.userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to assign task " + taskId);

        try {
            taskService.assignTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/delegate/{id}", method = RequestMethod.POST)
    public MessageResource delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody Long delegatedId) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if( !loggedUser.isAdmin() && !taskAuthenticationService.userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM, RolePermission.DELEGATE))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to delegate task " + taskId);

        try {
            taskService.delegateTask(loggedUser, delegatedId, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " assigned to [" + delegatedId + "]");
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return MessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    public MessageResource finish(Authentication auth, @PathVariable("id") String taskId) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if(	!loggedUser.isAdmin()
			&& !(
				taskAuthenticationService.userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM)
				&& taskAuthenticationService.isAssignee(loggedUser, taskId)
			))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to finish task " + taskId);

        try {
            taskService.finishTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " finished");
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.GET)
    public MessageResource cancel(Authentication auth, @PathVariable("id") String taskId) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

		if(	!loggedUser.isAdmin()
			&& !(
				taskAuthenticationService.userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM, RolePermission.CANCEL)
				&& taskAuthenticationService.isAssignee(loggedUser, taskId)
			))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to cancel task " + taskId);

        try {
            taskService.cancelTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " canceled");
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, com.netgrif.workflow.workflow.domain.Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/my/finished", method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, com.netgrif.workflow.workflow.domain.Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public PagedResources<LocalisedTaskResource> search(Authentication auth, Pageable pageable, @RequestBody Map<String, Object> searchBody, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
//        Page<LocalisedTask> page = null;
//        if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_1) {
//            page = taskService.findByPetriNets(pageable, searchBody.petriNets
//                    .stream()
//                    .map(net -> net.petriNet)
//                    .collect(Collectors.toList()));
//        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_2) {
//            List<String> transitions = new ArrayList<>();
//            searchBody.petriNets.forEach(net -> transitions.addAll(net.transitions));
//            page = taskService.findByTransitions(pageable, transitions);
//        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_3) {
//            //TODO: 4.6.2017 vyhľadanie na základe dát
//        }
        Page<com.netgrif.workflow.workflow.domain.Task> tasks = taskService.search(searchBody, pageable, (LoggedUser) auth.getPrincipal());
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, com.netgrif.workflow.workflow.domain.Task.class, selfLink.getRel());
        return resources;
    }

    @PostMapping(value = "/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CountResponse count(@RequestBody Map<String, Object> query, Authentication auth, Locale locale){
        long count = taskService.count(query, (LoggedUser)auth.getPrincipal(), locale);
        return CountResponse.taskCount(count);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET)
    public DataGroupsResource getData(@PathVariable("id") String taskId, Locale locale) {
        List<Field> dataFields = dataService.getData(taskId);
        List<DataGroup> dataGroups = dataService.getDataGroups(taskId);

        dataGroups.forEach(group -> group.setFields(new DataFieldsResource(dataFields.stream().filter(field ->
                group.getData().contains(field.getStringId())).collect(Collectors.toList()), locale)));

        return new DataGroupsResource(dataGroups, locale);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST)
    public ChangedFieldContainer saveData(@PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) throws UnauthorisedRequestException {
    	User logged = userService.getLoggedUser();
    	if( !logged.transformToLoggedUser().isAdmin() && !taskAuthenticationService.isAssignee(logged, taskId))
    		throw new UnauthorisedRequestException("User " + logged.transformToLoggedUser().getUsername() + " doesn't have permission to save data in task " + taskId);

        return dataService.setData(taskId, dataBody);
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.POST)
    public MessageResource saveFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                    @RequestParam(value = "file") MultipartFile multipartFile) throws UnauthorisedRequestException {
		User logged = userService.getLoggedUser();
		if( !logged.transformToLoggedUser().isAdmin() && !taskAuthenticationService.isAssignee(logged, taskId))
			throw new UnauthorisedRequestException("User " + logged.transformToLoggedUser().getUsername() + " doesn't have permission to save file in task " + taskId);

        if (dataService.saveFile(taskId, fieldId, multipartFile))
            return MessageResource.successMessage("File " + multipartFile.getOriginalFilename() + " successfully uploaded");
        else
            return MessageResource.errorMessage("File " + multipartFile.getOriginalFilename() + " failed to upload");
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId);

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }
}