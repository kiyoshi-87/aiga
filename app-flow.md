````
Home Page
│
| User enters media URL / selects upload
│
▼
[ Start Watch Party ]
│
▼
POST /api/rooms
│
│ Authorization: Host's session
│ Body: media information
▼
Spring Boot
│
├── Validate media
├── Create Room
├── Create Media record
├── Assign authenticated user as HOST
└── Initialize PlaybackState
│
▼
PostgreSQL
│
▼
201 Created
│
│ {
│   roomId,
│   shareUrl
│ }
▼
Frontend
│
▼
Navigate to /room/{roomId}
│
▼
Open WebSocket
│
▼
JOIN_ROOM
│
▼
Server validates host
│
▼
ROOM_STATE
│
▼
Host sees video
````