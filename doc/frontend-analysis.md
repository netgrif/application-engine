# Classes

# Cases
Class representing case (instance) of process

- Extends :: HalResource

  ## Config
  //TODO doplniť čo všetko môže byť v configu

  ## Methods
- constructor - host Tab, panel to render, case resource from backend, links, angular dependecies, config object
- load - load case data (http request)
- open - open case's taks in new Tab
- delete - delete whole forever (http request)

--------------------------------------------------------------------------------

# Action Cases
Class representing special case type with slightly different behaviour

- Extends :: Case

  ## Methods
- constructor - host Tab, host panel group to expansion panels, case resource, angular dependecies, config object
- loadData - load all case's data to expansion panel
- loadTasks - load all tasks that are available for this case
- click
- preventDefault
- expand
- panelExpand
- collapse
- openTaskDialog
- updateImmediateData
- removePanel

--------------------------------------------------------------------------------

# Case Tab
Class representing tab in case view which handles operations over cases. It is host for cases objects.

- Extends :: Tab

  ## Constants
- URL_SEARCH = "/res/workflow/case/search"
- FIND_BY_AUTHOR = 0
- FIND_BY_PETRINET = 1
- FIND_BY_TRANSITION = 2

  ## Methods
- constructor - label of Tab, controller, angular dependecies, config object
- activate
- buildSearchRequest
- load
- parseCase
- openCase
- closeCase
- openNewCaseDialog
- createCase
- loadPetriNets
- loadPetriNet
- loadTransitions
- delete

--------------------------------------------------------------------------------

# DataField
Class representing data field object of a process

- Extends :: HalResource

  ## Methods
- constructor - parent task, resource of data field, links, angular dependecies
- format
- isValid
- parse
- save
- chooseUser
- fileChanged
- upload
- download
- bindElement

  ### Static Methods
- padding
- roundToTwo

--------------------------------------------------------------------------------

# HalResource
Class to represent resourse recieved from backend server in HAL format.

## Methods
- constructor - links
- link

--------------------------------------------------------------------------------

# Tab
Class representing object of Tab in UI of web page.

## Methods
- constructor - id of tab in tabs array, label to show in UI
- activate

--------------------------------------------------------------------------------

# Task
Class representing task object created by backend server and represents enabled transition from process.

- Extends :: HalResource

  ## Config
  //TODO

  ## Methods
- constructor - host tab, panel as UI element, resource from server, links, angular dependecies, config object
- status
- assign
- delegate
- cancel
- doFinish
- finish
- getData
- load
- sortData
- validateRequiredData
- save
- updateData
- focusNearestRequiredField
- changeResource
- click
- preventDefault
- expand
- panelExpand
- collapse
- panelCollapse
- showDataGroupDivider
- getIcons

  ### Static Methods
- formatDate

--------------------------------------------------------------------------------

# TaskTab
Class representing tab in task view which handles operations over tasks. It is host for tasks objects.

- Extends :: Tab

  ## Constants
- URL_ALL = "/res/task"
- URL_MY = "/res/task/my"
- URL_SEARCH = "/res/task/search"
- URL_BYCASE = "/res/task/case"
- FIND_BY_CASE = 0
- FIND_BY_TITLE = 1

  ## Methods
- constructor - id of tab in tabs array, label of tab in UI, baseUrl to send request to, baseCriteria to apply search, useCase from which task are from, angular dependecies, config object
- activate
- reload
- buildRequestConfig
- load
- getSearchQuery
- parseTasks
- deleteTaskOnIndex
- prioritySort
- autoExpandTask
- updateTasksData
- loadTransactions
- mostForwardTransaction
- addTaskController
- removeAll
- expandTask
- reloadUseCase

--------------------------------------------------------------------------------

# Transaction
Class to represent group of transitions names as transaction.

## Methods
- constructor - resource sent from server, angular dependecies
- setActives

--------------------------------------------------------------------------------

# Services

# Auth
Service to handle authentication to authentication server.

## Constants
- loginPath = "/login"
- userPath = "user"
- logoutPath = "/logout"
- signupPath = "/signup"
- appPath = "/"

  ## Methods
- authenticate
- logout
- signup
- init
- isExcluded

--------------------------------------------------------------------------------

# AuthHttpInterceptor
Service to catch 401 responses to automaticaly logout user.

## Constants
- loginPath = "/login"
- signupPath = "/signup"

## Methods
- responseError

---

# CacheService
Service to act as temp storage for application components.

## Methods
- get
- put
- remove

---

# Dialog
Service to build and show dialogs.

## Methods
- showByTemplate
- show
- showByElement
- closeCurrent
- addCallback

---

# FileUpload
Service to upload file with meta data about to file.

## Methods
- upload

---

# Loading
Service to handle showing of main loading animation

## Methods
- setMainControllerCallback
- showLoading

---

# Localization
Service to handle i18n functionality across application.
This service host i18n object with all translations.

## Methods
- change
- current

---

# Snackbar
Service to build and show snackbar notifications.

## Constants
- delay = 3000
- position = "bottom right"
- infoTemplate = "<md-toast><div class='md-toast-content'><span class='md-toast-text' flex>{{msg}}</span></div></md-toast>"
- errorTemplate = "<md-toast><div class='md-toast-content'><span class='md-toast-text' flex>{{msg}}</span><md-icon class='material-icons cursor-pointer color-fg-error' ng-click='close()'>close</md-icon></div></md-toast>"
- successTemplate = "<md-toast><div class='md-toast-content'><span class='md-toast-text' flex>{{msg}}</span><md-icon class='material-icons color-fg-success'>done</md-icon></div></md-toast>"

## Private methods
- buildSnackbar

## Methods
- show
- simple
- error
- info
- success
- hide

---

# User
Service to host logged user object.

## Methods
- clear
- changeRoles
- hasAuthority
- hasRole
- canDo
- hasPermission
- getAsObject

---
