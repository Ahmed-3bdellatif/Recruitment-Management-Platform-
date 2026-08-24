package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleBasedCvParserTest {

    private RuleBasedCvParser parser;

    @BeforeEach
    void setUp() {
        parser = new RuleBasedCvParser();
    }

    @Test
    void parseExtractsCandidateDetailsFromResumeText() {
        String resumeText = """
                Alex Johnson
                alex.johnson@example.com | +1 (555) 234-5678 | San Francisco, CA
                https://linkedin.com/in/alex-johnson-dev
                https://github.com/alexj-dev

                SUMMARY:
                Results-driven Senior Software Engineer with 6+ years of experience building scalable backend microservices and distributed systems.

                EXPERIENCE:
                Senior Software Engineer | TechCorp Inc. (2020 - Present)
                - Architected microservices with Spring Boot, Java, and Kafka.
                - Designed PostgreSQL and Redis databases handling 10k requests/sec.
                - Implemented Docker and Kubernetes CI/CD pipelines on AWS.

                Software Engineer | DevHub (2018 - 2020)
                - Built RESTful APIs using Python, Django, and React.

                EDUCATION:
                Bachelor of Science in Computer Science, University of California (2014 - 2018)

                SKILLS:
                Java, Spring Boot, Microservices, Python, React, Docker, Kubernetes, AWS, PostgreSQL, Redis, Kafka, Git
                """;

        ParsedCvData data = parser.parse(resumeText);

        assertNotNull(data);
        assertEquals("Alex Johnson", data.getFullName());
        assertEquals("alex.johnson@example.com", data.getEmail());
        assertEquals("+1 (555) 234-5678", data.getPhone());
        assertEquals("https://linkedin.com/in/alex-johnson-dev", data.getLinkedinUrl());
        assertEquals("https://github.com/alexj-dev", data.getGithubUrl());
        assertEquals("Senior Software Engineer", data.getCurrentTitle());
        assertEquals(new BigDecimal("6.0"), data.getYearsOfExperience());
        assertTrue(data.getSkills().contains("Java"));
        assertTrue(data.getSkills().contains("Spring Boot"));
        assertTrue(data.getSkills().contains("Docker"));
        assertTrue(data.getSkills().contains("Kubernetes"));
        assertTrue(data.getSkills().contains("AWS"));
        assertTrue(data.getSkills().contains("PostgreSQL"));
        assertNotNull(data.getEducation());
        assertTrue(data.getEducation().contains("Computer Science"));
        assertEquals("RULE_BASED", data.getParserEngine());
    }

    @Test
    void parseHandlesEmptyTextGracefully() {
        ParsedCvData data = parser.parse("");
        assertNotNull(data);
        assertEquals("RULE_BASED", data.getParserEngine());
    }

    @Test
    void parseExtractsModernSkillsAndEngineeringDegrees() {
        String resume = """
                PROFILE
                Marcus Vance
                marcus.vance@testmail.com | +44 20 7946 0958
                https://linkedin.com/in/marcus-vance
                https://github.com/marcus-v
                Location: London, UK

                WORK EXPERIENCE
                Lead Software Engineer (2019 - Present)
                - Built cloud platforms using Kotlin, Spring Boot, GraphQL, and Redis.
                - Designed event-driven architecture with Kafka and RabbitMQ.
                - Frontend web applications with TypeScript, React, Next.js, and Tailwind CSS.
                - Container orchestration on GCP using Kubernetes and Terraform.

                ACADEMIC BACKGROUND
                B.Tech in Computer Engineering, Imperial College London (2015 - 2019)

                TECHNICAL SKILLS
                Kotlin, TypeScript, React, Next.js, Tailwind CSS, GraphQL, Redis, Kafka, RabbitMQ, GCP, Kubernetes, Terraform, PostgreSQL, Docker
                """;

        ParsedCvData data = parser.parse(resume);

        assertNotNull(data);
        assertEquals("Marcus Vance", data.getFullName());
        assertEquals("marcus.vance@testmail.com", data.getEmail());
        assertEquals("Lead Software Engineer", data.getCurrentTitle());
        assertEquals("London, UK", data.getLocation());
        assertTrue(data.getSkills().contains("Kotlin"));
        assertTrue(data.getSkills().contains("TypeScript"));
        assertTrue(data.getSkills().contains("React"));
        assertTrue(data.getSkills().contains("Next.js"));
        assertTrue(data.getSkills().contains("GraphQL"));
        assertTrue(data.getSkills().contains("Redis"));
        assertTrue(data.getSkills().contains("Kafka"));
        assertTrue(data.getSkills().contains("GCP"));
        assertTrue(data.getSkills().contains("Terraform"));
        assertNotNull(data.getEducation());
        assertTrue(data.getEducation().contains("B.Tech in Computer Engineering"));
    }
}
