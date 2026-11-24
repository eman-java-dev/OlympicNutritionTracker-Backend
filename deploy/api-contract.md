# API authorization and validation

| Surface | ADMIN | COACH | ATHLETE |
| --- | --- | --- | --- |
| Athlete list/detail | All | Assigned athletes | Linked athlete |
| Athlete writes and coach assignment | Allowed | Denied | Denied |
| Coach directory | Authenticated read | Authenticated read | Authenticated read |
| Coach writes | Allowed | Denied | Denied |
| Nested coach athletes | All athletes assigned to that coach | Own scoped records | Own linked record when assigned to that coach |
| Nutrition read | All | Assigned athletes | Linked athlete |
| Nutrition create/update | Allowed | Assigned athletes | Denied |
| Nutrition delete | Allowed | Denied | Denied |
| Consultation read | All | Own appointments for assigned athletes | Linked athlete |
| Consultation create/update | Allowed | Own coach profile and assigned athletes | Denied |
| Consultation delete | Allowed | Denied | Denied |

Anonymous resource requests are denied. An unlinked profile has an empty scope. Direct IDs are resolved within the caller's scope and a coach write checks the target athlete before any mutation. The directory remains available for authenticated unlinked accounts. A request uses the database role fetched during token authentication. A write that reaches its record-scope check after a committed reassignment must use the new scope. The regression pauses an in-flight coach request before that check and verifies that its record stays unchanged. Concurrent writes that have already passed the scope check remain outside this proof.

| Request | Field contract |
| --- | --- |
| Registration | Existing username rules. New password of at least 15 Unicode code points and at most 72 UTF-8 bytes. |
| Login | Existing correct passwords remain accepted. Username is bounded by the mapped column and BCrypt input retains its byte ceiling. |
| Athlete | Name required up to 120. Optional gender up to 10. Optional age is a positive integer and height/weight are finite positive measurements consistent with the UI. |
| Nutrition | Date and positive athlete reference required. Macros and calories remain nullable and accept zero or positive integers. Optional notes up to 300. |
| Coach | Preserve existing required name and optional specialty, email and phone validation. |
| Consultation | Preserve required athlete/coach IDs, scheduled date/time and nonblank message up to 300. Historical dates remain supported within the MySQL DATE/DATETIME range (years 1000 through 9999). |

The account throttle counts valid request bodies, including successful operations, in separate fixed windows for login and registration. Defaults are 10 login and 5 registration attempts per minute per account bucket. A bounded 10,000-entry map retains existing buckets, with per-operation overflow windows for new keys during saturation. It resets on process restart and is local to the instance. Case, surrounding whitespace and combining marks are folded only for the bucket and do not change username lookup. This folding can group different accounts and does not cover every database collation equivalence. Throttled requests return HTTP 429 and a `Retry-After` delay in seconds. Same-account requests can share an exhausted window until expiry. Confirm a trusted source identity and topology separately before adding source-based enforcement.
