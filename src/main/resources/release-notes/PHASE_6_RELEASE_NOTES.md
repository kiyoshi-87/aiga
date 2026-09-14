# Phase 6 Release Notes

## What changed

- Added server-authoritative video playback over WebSockets.
- Added `PLAY`, `PAUSE`, `SEEK`, and `PLAYBACK_UPDATED` protocol messages.
- Added optional-authentication WebSocket connections and host-only playback permissions.
- Added room-scoped in-memory playback state with automatic cleanup when the last participant leaves.
- Added a dynamic watch-room player at `/room/{roomId}`.

## Now functional

- Hosts can play, pause, and seek video for everyone in the room.
- Authenticated and anonymous guests receive server broadcasts and follow the host's playback state.
- New participants receive the current playback state as soon as they join.
- Guests cannot use video controls or submit playback commands.
- Server-driven playback changes do not trigger client command loops.

## Configuration

The frontend reads the signed-in user's token from `localStorage.accessToken` and connects to the API URL in `NEXT_PUBLIC_API_URL` (default: `http://localhost:8080`).
