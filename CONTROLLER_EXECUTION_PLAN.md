# Controller Implementation Execution Plan

## Overview
Build REST controllers for all DTOs created (Job, Application, Interview, InterviewFeedback, HiringDecision)

---

## Phase 1: JobController
**Endpoints:**
- `GET /api/jobs` - Get all jobs (with pagination/filtering)
- `GET /api/jobs/{id}` - Get job by ID
- `POST /api/jobs` - Create new job
- `PUT /api/jobs/{id}` - Update job
- `DELETE /api/jobs/{id}` - Delete job
- `PUT /api/jobs/{id}/publish` - Publish job (change status)
- `PUT /api/jobs/{id}/close` - Close job (change status)

**Key Logic:**
- Only recruiters/admins can create/update/delete jobs
- CreatedBy user must be set from authentication context
- Validate job status transitions

---

## Phase 2: ApplicationController
**Endpoints:**
- `GET /api/applications` - Get all applications (with filtering by candidate/job/status)
- `GET /api/applications/{id}` - Get application by ID
- `POST /api/applications` - Create new application
- `PUT /api/applications/{id}` - Update application
- `DELETE /api/applications/{id}` - Delete application
- `PUT /api/applications/{id}/assign` - Assign recruiter to application

**Key Logic:**
- Check duplicate applications (same candidate + job)
- Validate job is published before allowing application
- Update application status based on interview/hiring decision flow

---

## Phase 3: InterviewController
**Endpoints:**
- `GET /api/interviews` - Get all interviews (with filtering)
- `GET /api/interviews/{id}` - Get interview by ID
- `POST /api/interviews` - Schedule new interview
- `PUT /api/interviews/{id}` - Update interview
- `DELETE /api/interviews/{id}` - Cancel interview
- `PUT /api/interviews/{id}/complete` - Mark interview as completed

**Key Logic:**
- Only assigned recruiters can create interviews
- Validate scheduled time is in future
- Check application status before scheduling interview
- Update application status when interview is completed

---

## Phase 4: InterviewFeedbackController
**Endpoints:**
- `GET /api/interviews/{interviewId}/feedback` - Get feedback for interview
- `POST /api/interviews/{interviewId}/feedback` - Submit feedback
- `PUT /api/feedback/{id}` - Update feedback
- `DELETE /api/feedback/{id}` - Delete feedback

**Key Logic:**
- Only interviewer can submit/update feedback
- Feedback can only be submitted after interview is completed
- Calculate overall score if not provided
- Update application status based on feedback scores

---

## Phase 5: HiringDecisionController
**Endpoints:**
- `GET /api/hiring-decisions` - Get all hiring decisions (with filtering)
- `GET /api/hiring-decisions/{id}` - Get hiring decision by ID
- `POST /api/hiring-decisions` - Make hiring decision
- `PUT /api/hiring-decisions/{id}` - Update hiring decision
- `DELETE /api/hiring-decisions/{id}` - Delete hiring decision

**Key Logic:**
- Only authorized users (managers/admins) can make decisions
- One decision per application (unique constraint)
- Update application status based on decision (ACCEPTED/REJECTED)
- Log decision with reason

---

## Common Controller Features

### For All Controllers:
```
1. Exception Handling
   - Handle @Valid validation errors
   - Handle EntityNotFoundException
   - Return appropriate HTTP status codes (200, 201, 400, 404, 409, 500)

2. Authentication/Authorization
   - Add @PreAuthorize or @Secured annotations
   - Extract current user from SecurityContext
   - Verify user permissions

3. Response Format
   - Wrap responses in ApiResponse<T> object (create separate class)
   - Include timestamp, status, message, data

4. Pagination & Filtering
   - Implement PageRequest for list endpoints
   - Add filter parameters (status, dateRange, etc.)

5. Logging
   - Log all CRUD operations
   - Log authorization failures
```

### ApiResponse Wrapper (create new class):
```java
@Getter
@Builder
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;
    private List<String> errors;
}
```

---

## Implementation Order (Recommended)
1. **JobController** - Independent, no complex dependencies
2. **ApplicationController** - Depends on JobController logic
3. **InterviewController** - Depends on ApplicationController
4. **InterviewFeedbackController** - Depends on InterviewController
5. **HiringDecisionController** - Depends on ApplicationController
6. **Global Exception Handler** - Used by all controllers

---

## Technology Stack to Use
- `@RestController` - Define controller
- `@RequestMapping` - Base path for controller
- `@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping` - HTTP methods
- `@Valid` - Enable validation on request body
- `@PathVariable` - Extract path parameters
- `@RequestParam` - Query parameters
- `@PreAuthorize` - Role-based access control
- `ResponseEntity<T>` - HTTP responses with status codes

---

## Status Codes to Use
| Code | Scenario |
|------|----------|
| 200  | GET, PUT successful |
| 201  | POST (resource created) |
| 204  | DELETE successful |
| 400  | Validation error |
| 401  | Authentication required |
| 403  | Authorization failed |
| 404  | Resource not found |
| 409  | Conflict (duplicate, invalid state) |
| 500  | Server error |

---

## Next Steps
1. Implement GlobalExceptionHandler
2. Create ApiResponse wrapper class
3. Implement controllers in order (Phase 1 → Phase 5)
4. Test each controller endpoint
5. Add integration tests
