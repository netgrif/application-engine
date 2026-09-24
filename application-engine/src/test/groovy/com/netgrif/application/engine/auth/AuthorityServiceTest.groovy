package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.config.AuthorityConfigurationProperties
import com.netgrif.application.engine.auth.repository.AuthorityRepository
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.dto.AuthoritySearchDto
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AuthorityServiceTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private AuthorityRepository authorityRepository

    @Autowired
    private AuthorityConfigurationProperties authorityConfigurationProperties

    @Autowired
    private CacheManager cacheManager

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        cacheManager.cacheNames.each { name ->
            cacheManager.getCache(name)?.clear()
        }
    }

    @Test
    void testGetOrCreate_WhenAuthorityDoesNotExist_CreatesAndReturnsNew() {
        long initialCount = authorityRepository.count()

        Authority created = authorityService.getOrCreate("ROLE_TEST_CREATE")

        assertNotNull(created)
        assertEquals("ROLE_TEST_CREATE", created.getName())
        assertEquals("ROLE_TEST_CREATE", created.getAuthority())
        assertNotNull(created.get_id())
        assertNotNull(created.getStringId())
        assertEquals(initialCount + 1, authorityRepository.count())

        Optional<Authority> found = authorityRepository.findByName("ROLE_TEST_CREATE")
        assertTrue(found.isPresent())
        assertEquals(created.get_id(), found.get().get_id())
    }

    @Test
    void testGetOrCreate_WhenAuthorityExists_ReturnsExistingWithoutDuplicate() {
        Authority first = authorityService.getOrCreate("ROLE_DUPLICATE_CHECK")
        long countAfterFirst = authorityRepository.count()

        Authority second = authorityService.getOrCreate("ROLE_DUPLICATE_CHECK")

        assertNotNull(second)
        assertEquals(first.get_id(), second.get_id())
        assertEquals("ROLE_DUPLICATE_CHECK", second.getName())
        assertEquals(countAfterFirst, authorityRepository.count())
    }

    @Test
    void testGetOne_WhenExists_ReturnsAuthority() {
        Authority created = authorityService.getOrCreate("ROLE_GET_ONE")

        Authority fetched = authorityService.getOne(created.getStringId())

        assertNotNull(fetched)
        assertEquals(created.get_id(), fetched.get_id())
        assertEquals("ROLE_GET_ONE", fetched.getName())
    }

    @Test
    void testGetOne_WhenDoesNotExist_ThrowsIllegalArgumentException() {
        String nonExistingId = new ObjectId().toString()

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.getOne(nonExistingId)
        })

        assertTrue(exception.getMessage().contains("Authority with id " + nonExistingId + " not found"))
    }

    @Test
    void testFindAll_WithPagination() {
        long initialCount = authorityRepository.count()
        (1..5).each { i ->
            authorityService.getOrCreate("ROLE_PAGED_${i}")
        }
        long totalExpected = initialCount + 5

        Page<Authority> page1 = authorityService.findAll(PageRequest.of(0, 2))
        assertEquals(2, page1.getContent().size())
        assertEquals(totalExpected, page1.getTotalElements())

        Page<Authority> page2 = authorityService.findAll(PageRequest.of(1, 2))
        assertEquals(2, page2.getContent().size())
        assertNotEquals(page1.getContent().get(0).getName(), page2.getContent().get(0).getName())

        Page<Authority> unpaged = authorityService.findAll(Pageable.unpaged())
        assertEquals(totalExpected, unpaged.getContent().size())
    }

    @Test
    void testFindAllByIds() {
        Authority auth1 = authorityService.getOrCreate("ROLE_ID_1")
        Authority auth2 = authorityService.getOrCreate("ROLE_ID_2")
        authorityService.getOrCreate("ROLE_ID_3")

        Page<Authority> result = authorityService.findAllByIds([auth1.getStringId(), auth2.getStringId()], PageRequest.of(0, 10))

        assertEquals(2, result.getTotalElements())
        Set<String> names = result.getContent().collect { it.getName() } as Set
        assertTrue(names.contains("ROLE_ID_1"))
        assertTrue(names.contains("ROLE_ID_2"))
        assertFalse(names.contains("ROLE_ID_3"))

        Page<Authority> emptyResult = authorityService.findAllByIds([new ObjectId().toString()], PageRequest.of(0, 10))
        assertEquals(0, emptyResult.getTotalElements())
    }

    @Test
    void testSearch_WithFullText() {
        authorityService.getOrCreate("PROCESS_CREATE")
        authorityService.getOrCreate("PROCESS_DELETE")
        authorityService.getOrCreate("USER_CREATE")
        authorityService.getOrCreate("OTHER_PERMISSION")

        AuthoritySearchDto searchDto = new AuthoritySearchDto()
        searchDto.setFullText("process")

        Page<Authority> searchResult = authorityService.search(searchDto, PageRequest.of(0, 10))
        assertEquals(2, searchResult.getTotalElements())
        assertTrue(searchResult.getContent().every { it.getName().startsWith("PROCESS_") })

        searchDto.setFullText("create")
        Page<Authority> createResult = authorityService.search(searchDto, PageRequest.of(0, 10))
        assertEquals(2, createResult.getTotalElements())
        Set<String> createNames = createResult.getContent().collect { it.getName() } as Set
        assertTrue(createNames.contains("PROCESS_CREATE"))
        assertTrue(createNames.contains("USER_CREATE"))
    }

    @Test
    void testSearch_WithEmptyOrNullFullText() {
        long initialCount = authorityRepository.count()
        authorityService.getOrCreate("AUTH_1")
        authorityService.getOrCreate("AUTH_2")
        long totalExpected = initialCount + 2

        AuthoritySearchDto emptyDto = new AuthoritySearchDto()
        emptyDto.setFullText("")
        Page<Authority> emptyResult = authorityService.search(emptyDto, PageRequest.of(0, 100))
        assertEquals(totalExpected, emptyResult.getTotalElements())

        AuthoritySearchDto blankDto = new AuthoritySearchDto()
        blankDto.setFullText("   ")
        Page<Authority> blankResult = authorityService.search(blankDto, PageRequest.of(0, 100))
        assertEquals(totalExpected, blankResult.getTotalElements())

        AuthoritySearchDto nullDto = new AuthoritySearchDto()
        nullDto.setFullText(null)
        Page<Authority> nullResult = authorityService.search(nullDto, PageRequest.of(0, 100))
        assertEquals(totalExpected, nullResult.getTotalElements())
    }

    @Test
    void testDelete_ExistingAuthority() {
        authorityService.getOrCreate("ROLE_TO_DELETE")
        assertTrue(authorityService.findOptionalByName("ROLE_TO_DELETE").isPresent())

        authorityService.delete("ROLE_TO_DELETE")

        assertTrue(authorityService.findOptionalByName("ROLE_TO_DELETE").isEmpty())
    }

    @Test
    void testDelete_NonExistingAuthority_DoesNotThrow() {
        authorityService.delete("ROLE_DOES_NOT_EXIST")
    }

    @Test
    void testDelete_ScopedAuthorityName_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.delete("PROCESS_*")
        })
        assertEquals("The authority name is not valid. Scope is suitable for this function.", exception.getMessage())
    }

    @Test
    void testDelete_InvalidScopeFormat_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.delete("PRO*CESS")
        })
        assertEquals("The authority name or scope is not valid.", exception.getMessage())
    }

    @Test
    void testFindByName_WhenExists_ReturnsAuthority() {
        authorityService.getOrCreate("ROLE_FIND_BY_NAME")

        Authority found = authorityService.findByName("ROLE_FIND_BY_NAME")

        assertNotNull(found)
        assertEquals("ROLE_FIND_BY_NAME", found.getName())
    }

    @Test
    void testFindByName_WhenNotFound_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.findByName("NON_EXISTING_AUTH")
        })
        assertEquals("Could not find authority with name [NON_EXISTING_AUTH]", exception.getMessage())
    }

    @Test
    void testFindByName_ScopedName_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.findByName("ROLE_*")
        })
        assertEquals("The authority name is not valid. Scope is suitable for this function.", exception.getMessage())
    }

    @Test
    void testFindByName_InvalidScopeFormat_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.findByName("ROLE_*_ADMIN")
        })
        assertEquals("The authority name or scope is not valid.", exception.getMessage())
    }

    @Test
    void testFindOptionalByName() {
        authorityService.getOrCreate("OPTIONAL_PRESENT")

        Optional<Authority> present = authorityService.findOptionalByName("OPTIONAL_PRESENT")
        assertTrue(present.isPresent())
        assertEquals("OPTIONAL_PRESENT", present.get().getName())

        Optional<Authority> missing = authorityService.findOptionalByName("OPTIONAL_MISSING")
        assertTrue(missing.isEmpty())
    }

    @Test
    void testFindByScope_WildcardAll() {
        long initialCount = authorityRepository.count()
        authorityService.getOrCreate("AUTH_A")
        authorityService.getOrCreate("AUTH_B")
        authorityService.getOrCreate("AUTH_C")
        long totalExpected = initialCount + 3

        List<Authority> all = authorityService.findByScope("*")

        assertEquals(totalExpected, all.size())
        Set<String> names = all.collect { it.getName() } as Set
        assertTrue(names.containsAll(["AUTH_A", "AUTH_B", "AUTH_C"]))
    }

    @Test
    void testFindByScope_PrefixScope() {
        authorityService.getOrCreate("TASK_READ")
        authorityService.getOrCreate("TASK_WRITE")
        authorityService.getOrCreate("TASK_DELETE")
        authorityService.getOrCreate("USER_READ")

        List<Authority> taskAuthorities = authorityService.findByScope("TASK_*")

        assertEquals(3, taskAuthorities.size())
        Set<String> names = taskAuthorities.collect { it.getName() } as Set
        assertTrue(names.containsAll(["TASK_READ", "TASK_WRITE", "TASK_DELETE"]))
        assertFalse(names.contains("USER_READ"))

        List<Authority> emptyScope = authorityService.findByScope("UNKNOWN_SCOPE_*")
        assertTrue(emptyScope.isEmpty())
    }

    @Test
    void testFindByScope_ExactAuthorityName() {
        authorityService.getOrCreate("SINGLE_AUTH")

        List<Authority> singleList = authorityService.findByScope("SINGLE_AUTH")

        assertEquals(1, singleList.size())
        assertEquals("SINGLE_AUTH", singleList.get(0).getName())
    }

    @Test
    void testFindByScope_InvalidScopeFormat_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.findByScope("IN*VALID")
        })
        assertEquals("The authority name or scope is not valid.", exception.getMessage())
    }

    @Test
    void testFindByScope_NonExistentExactName_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, {
            authorityService.findByScope("NON_EXISTENT_EXACT_NAME")
        })
        assertEquals("Could not find authority with name [NON_EXISTENT_EXACT_NAME]", exception.getMessage())
    }

    @Test
    void testGetDefaultUserAuthorities() {
        authorityService.getOrCreate("USER_READ")
        authorityService.getOrCreate("USER_WRITE_OWN")
        authorityService.getOrCreate("USER_WRITE_ALL")
        authorityService.getOrCreate("ADMIN_ALL")

        authorityConfigurationProperties.setDefaultUserAuthorities(["USER_READ", "USER_WRITE_*"])

        Set<Authority> defaultAuthorities = authorityService.getDefaultUserAuthorities()

        assertNotNull(defaultAuthorities)
        assertEquals(3, defaultAuthorities.size())
        Set<String> names = defaultAuthorities.collect { it.getName() } as Set
        assertTrue(names.containsAll(["USER_READ", "USER_WRITE_OWN", "USER_WRITE_ALL"]))
        assertFalse(names.contains("ADMIN_ALL"))
    }

    @Test
    void testGetDefaultAnonymousAuthorities() {
        authorityService.getOrCreate("PUBLIC_VIEW")
        authorityService.getOrCreate("PUBLIC_EXPORT")
        authorityService.getOrCreate("INTERNAL_VIEW")

        authorityConfigurationProperties.setDefaultAnonymousAuthorities(["PUBLIC_*"])

        Set<Authority> defaultAuthorities = authorityService.getDefaultAnonymousAuthorities()

        assertNotNull(defaultAuthorities)
        assertEquals(2, defaultAuthorities.size())
        Set<String> names = defaultAuthorities.collect { it.getName() } as Set
        assertTrue(names.containsAll(["PUBLIC_VIEW", "PUBLIC_EXPORT"]))
        assertFalse(names.contains("INTERNAL_VIEW"))
    }

    @Test
    void testGetDefaultAdminAuthorities() {
        long initialCount = authorityRepository.count()
        authorityService.getOrCreate("ADMIN_DASHBOARD")
        authorityService.getOrCreate("ADMIN_USERS")
        authorityService.getOrCreate("CUSTOM_PERMISSION")
        long totalExpected = initialCount + 3

        authorityConfigurationProperties.setDefaultAdminAuthorities(["*"])

        Set<Authority> defaultAuthorities = authorityService.getDefaultAdminAuthorities()

        assertNotNull(defaultAuthorities)
        assertEquals(totalExpected, defaultAuthorities.size())
        Set<String> names = defaultAuthorities.collect { it.getName() } as Set
        assertTrue(names.containsAll(["ADMIN_DASHBOARD", "ADMIN_USERS", "CUSTOM_PERMISSION"]))
    }
}
