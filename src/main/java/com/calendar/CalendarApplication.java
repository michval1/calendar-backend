package com.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hlavná trieda kalendárnej aplikácie.
 *
 * <p>Toto je vstupný bod (entry point) pre Spring Boot aplikáciu.
 * Spúšťa celú aplikáciu vrátane všetkých konfigurácií, controllerov,
 * servisov a repozitárov.</p>
 *
 * <p>Aplikácia poskytuje:
 * <ul>
 *   <li>RESTful API pre správu udalostí a používateľov</li>
 *   <li>Podporu pre zdieľanie udalostí medzi používateľmi</li>
 *   <li>Systém pripomienok</li>
 *   <li>Firebase autentifikáciu</li>
 *   <li>Admin panel pre správu systému</li>
 * </ul></p>
 *
 * <p>Technológie:
 * <ul>
 *   <li>Spring Boot 3.x</li>
 *   <li>Spring Data JPA / Hibernate</li>
 *   <li>MySQL databáza</li>
 *   <li>Firebase Authentication</li>
 *   <li>React frontend</li>
 * </ul></p>
 *
 * <p>Aplikácia beží predvolene na porte 8080 a poskytuje API na:
 * <code>http://localhost:8080/api/v1</code></p>
 *
 * @author Michal Prosňanský a Ela Ledecká
 * @version 0.9
 */
@SpringBootApplication
public class CalendarApplication {

    /**
     * Hlavná metóda spúšťajúca aplikáciu.
     *
     * <p>Inicializuje Spring Boot kontext, načíta konfiguráciu
     * z application.properties a spustí embedded Tomcat server.</p>
     *
     * @param args argumenty príkazového riadku (momentálne nepoužívané)
     */
    public static void main(String[] args) {
        SpringApplication.run(CalendarApplication.class, args);
    }
}