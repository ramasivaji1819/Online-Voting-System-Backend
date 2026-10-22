# Online Voting System (Spring Boot + JWT + MySQL)

## Run in STS / Eclipse
1. Install MySQL and make sure it is running (database `voting_db` is auto-created).
2. Edit `src/main/resources/application.properties` -> set your MySQL username/password.
3. File > Import > Maven > Existing Maven Projects > select this folder.
4. Right-click project > Maven > Update Project (Alt+F5).
5. Right-click `OnlineVotingApplication.java` > Run As > Spring Boot App (Java 17+ required).

Default admin: `admin@voting.com` / `Admin@123`

## API
| Method | URL | Access |
|---|---|---|
| POST | /api/auth/register | public |
| POST | /api/auth/login | public |
| POST | /api/admin/elections | ADMIN |
| POST | /api/admin/elections/{id}/candidates | ADMIN (before election starts) |
| GET | /api/elections | logged in |
| GET | /api/elections/{id}/candidates | logged in |
| POST | /api/elections/{id}/vote | VOTER (election must be active) |
| GET | /api/elections/{id}/has-voted | logged in |
| GET | /api/elections/{id}/results | admin anytime, voters after end |

Send the token as header: `Authorization: Bearer <token>`

## Sample requests
Register: `{"name":"Ravi","email":"ravi@mail.com","password":"secret123"}`
Login: `{"email":"admin@voting.com","password":"Admin@123"}`
Create election: `{"title":"Class Rep","description":"Vote","startTime":"2026-10-17T09:00:00","endTime":"2026-10-30T18:00:00"}`
Add candidate: `{"name":"Alice","party":"Blue"}`
Vote: `{"candidateId":1}`

Tip: to test voting quickly, create an election with startTime a few minutes from now,
add candidates, wait until it starts, then vote.
