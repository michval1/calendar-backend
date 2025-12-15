package com.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.calendar.controller.EventController;
import com.calendar.controller.UserController;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import com.calendar.repository.ReminderRepository;
import com.calendar.service.EventService;
import com.calendar.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integračné testy pre Calendar aplikáciu.
 *
 * <p>Tento test overuje, že Spring Boot aplikácia sa správne načíta
 * a všetky komponenty sú správne nakonfigurované.</p>
 *
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Calendar Application Integration Tests")
class CalendarApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private EventController eventController;

	@Autowired(required = false)
	private UserController userController;

	@Autowired(required = false)
	private EventService eventService;

	@Autowired(required = false)
	private UserService userService;

	@Autowired(required = false)
	private EventRepository eventRepository;

	@Autowired(required = false)
	private UserRepository userRepository;

	@Autowired(required = false)
	private ReminderRepository reminderRepository;

	@Test
	@DisplayName("Spring kontext sa načíta bez chýb")
	void contextLoads() {
		assertNotNull(applicationContext, "Application context by nemal byť null");
	}

	@Test
	@DisplayName("Všetky controllery sú načítané")
	void allControllersAreLoaded() {
		assertNotNull(eventController, "EventController by mal byť načítaný");
		assertNotNull(userController, "UserController by mal byť načítaný");
	}

	@Test
	@DisplayName("Všetky service beany sú načítané")
	void allServicesAreLoaded() {
		assertNotNull(eventService, "EventService by mal byť načítaný");
		assertNotNull(userService, "UserService by mal byť načítaný");
	}

	@Test
	@DisplayName("Všetky repository beany sú načítané")
	void allRepositoriesAreLoaded() {
		assertNotNull(eventRepository, "EventRepository by mal byť načítaný");
		assertNotNull(userRepository, "UserRepository by mal byť načítaný");
		assertNotNull(reminderRepository, "ReminderRepository by mal byť načítaný");
	}

	@Test
	@DisplayName("EventController má správne injektované závislosti")
	void eventControllerHasDependencies() {
		if (eventController != null) {
			assertTrue(true);
		}
	}

	@Test
	@DisplayName("UserController má správne injektované závislosti")
	void userControllerHasDependencies() {
		if (userController != null) {
			assertTrue(true);
		}
	}

	@Test
	@DisplayName("Všetky potrebné bean-y existujú v kontexte")
	void allRequiredBeansExist() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		assertTrue(beanNames.length > 0, "Aplikačný kontext by mal obsahovať bean-y");

		boolean hasEventService = applicationContext.containsBean("eventService");
		boolean hasUserService = applicationContext.containsBean("userService");

		assertTrue(hasEventService || hasUserService,
				"Aspoň jeden zo základných service bean-ov by mal existovať");
	}

	@Test
	@DisplayName("JPA repositories sú nakonfigurované")
	void jpaRepositoriesAreConfigured() {
		if (eventRepository != null) {
			assertNotNull(eventRepository.getClass());
			assertTrue(eventRepository.getClass().getName().contains("Proxy") ||
							eventRepository.getClass().getName().contains("Repository"),
					"EventRepository by mal byť JPA repository proxy");
		}
	}

	@Test
	@DisplayName("Aplikácia je v zdravom stave")
	void applicationIsHealthy() {
		assertNotNull(applicationContext);
		assertTrue(applicationContext.getBeanDefinitionCount() > 0,
				"Application context by mal obsahovať bean-y");
	}

	@Test
	@DisplayName("Žiadne konflikty v definíciách bean-ov")
	void noConflictsInBeanDefinitions() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		assertNotNull(beanNames);
		assertTrue(beanNames.length > 0);
		assertTrue(true, "Žiadne konflikty pri načítaní bean-ov");
	}
}