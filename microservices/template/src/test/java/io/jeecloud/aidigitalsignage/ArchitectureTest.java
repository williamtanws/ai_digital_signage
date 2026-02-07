package io.jeecloud.aidigitalsignage;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests to enforce Package by Component with Hexagonal Architecture rules.
 * 
 * Rules enforced:
 * 1. Domain layer independence
 * 2. Application layer dependencies
 * 3. Component isolation
 * 4. Shared kernel usage
 * 5. Naming conventions
 */
@DisplayName("Architecture Tests - Package by Component")
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("io.jeecloud.aidigitalsignage");

    // ===== Layer Dependency Rules =====

    @Test
    @DisplayName("Domain layer should not depend on application or infrastructure")
    void domainLayerShouldNotDependOnOtherLayers() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
            .because("Domain layer must be independent from technical concerns")
            .check(classes);
    }

    @Test
    @DisplayName("Application layer should not depend on infrastructure")
    void applicationLayerShouldNotDependOnInfrastructure() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("Application layer should only depend on domain and ports")
            .check(classes);
    }

    @Test
    @DisplayName("Infrastructure layer can depend on application and domain")
    void infrastructureCanDependOnApplicationAndDomain() {
        classes()
            .that().resideInAPackage("..infrastructure..")
            .should().onlyAccessClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "..application..",
                "..domain..",
                "..shared..",
                "..common..",  // Allow access to common package (includes common.event)
                "java..",
                "org.springframework..",
                "jakarta..",
                "com.fasterxml..",
                "org.slf4j..",
                "org.apache..",
                "io.micrometer..",
                "io.swagger..",  // OpenAPI/Swagger annotations
                "lombok.."
            )
            .because("Infrastructure implements adapters for domain and application ports")
            .check(classes);
    }

    @Test
    @DisplayName("Layered architecture within components should be respected")
    void layeredArchitectureWithinComponentsShouldBeRespected() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .because("Hexagonal architecture dependency rules must be enforced")
            .check(classes);
    }

    // ===== Component Isolation Rules =====

    @Test
    @DisplayName("Components should not depend on other components (except shared)")
    void componentsShouldNotDependOnOtherComponents() {
        noClasses()
            .that().resideInAPackage("..agent..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..policy..",
                "..claim..",
                "..commission.."
            )
            .because("Components must be isolated - only shared kernel dependencies allowed")
            .check(classes);
    }

    @Test
    @DisplayName("All layers can depend on shared kernel")
    void allLayersCanDependOnSharedKernel() {
        classes()
            .that().resideInAnyPackage("..agent..", "..policy..", "..claim..")
            .should().onlyAccessClassesThat().resideInAnyPackage(
                "..agent..",
                "..policy..",
                "..claim..",
                "..common..",
                "java..",
                "org.springframework..",
                "jakarta..",
                "com.fasterxml..",
                "org.slf4j..",
                "org.apache..",
                "lombok.."
            )
            .because("Components can only depend on themselves and shared kernel")
            .check(classes);
    }

    @Test
    @DisplayName("Shared kernel should not depend on components")
    void sharedKernelShouldNotDependOnComponents() {
        noClasses()
            .that().resideInAPackage("..common..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..agent..",
                "..policy..",
                "..claim..",
                "..commission.."
            )
            .because("Shared kernel must remain generic and component-independent")
            .allowEmptyShould(true)
            .check(classes);
    }

    // ===== Spring Framework Rules =====

    @Test
    @DisplayName("Domain entities should not have Spring annotations (except @Service for domain services)")
    void domainEntitiesShouldNotHaveSpringAnnotations() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("DomainService")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .because("Domain layer should be framework-independent")
            .check(classes);
    }

    @Test
    @DisplayName("Domain entities should not have JPA annotations")
    void domainEntitiesShouldNotHaveJPAAnnotations() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
            .because("JPA annotations belong to infrastructure layer entities")
            .check(classes);
    }

    // ===== Naming Convention Rules =====

    @Test
    @DisplayName("Use case interfaces should end with 'UseCase'")
    void useCaseInterfacesShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("Input ports represent use cases")
            .check(classes);
    }

    @Test
    @DisplayName("Repository interfaces should end with 'Repository'")
    void repositoryInterfacesShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..domain..")
            .and().areInterfaces()
            .and().haveSimpleNameNotContaining("UseCase")
            .and().haveSimpleNameNotContaining("Event")
            .and().haveSimpleNameContaining("Repository")
            .should().haveSimpleNameEndingWith("Repository")
            .because("Domain repository ports should follow naming convention")
            .check(classes);
    }

    @Test
    @DisplayName("Application services should end with 'Service'")
    void applicationServicesShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..application.service..")
            .should().haveSimpleNameEndingWith("Service")
            .because("Application services should follow naming convention")
            .check(classes);
    }

    @Test
    @DisplayName("REST controllers should end with 'Controller'")
    void restControllersShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..infrastructure.rest..")
            .and().resideOutsideOfPackage("..rest.common..")
            .and().haveSimpleNameNotEndingWith("Handler")
            .should().haveSimpleNameEndingWith("Controller")
            .because("REST controllers should follow naming convention")
            .check(classes);
    }

    @Test
    @DisplayName("JPA entities should end with 'Entity'")
    void jpaEntitiesShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..infrastructure.persistence..")
            .and().areAnnotatedWith("jakarta.persistence.Entity")
            .should().haveSimpleNameEndingWith("Entity")
            .because("Infrastructure JPA entities should be clearly distinguished from domain entities")
            .check(classes);
    }

    @Test
    @DisplayName("Repository adapters should end with 'Adapter'")
    void repositoryAdaptersShouldFollowNamingConvention() {
        classes()
            .that().resideInAPackage("..infrastructure.persistence..")
            .and().haveSimpleNameNotEndingWith("Entity")
            .and().haveSimpleNameNotEndingWith("Repository")
            .and().haveSimpleNameContaining("Repository")
            .should().haveSimpleNameEndingWith("Adapter")
            .because("Repository implementations should be clearly marked as adapters")
            .check(classes);
    }
}



