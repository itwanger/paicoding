# Profile Dashboard Design

## Goal

Upgrade the miniapp profile tab into a dashboard-style personal center while keeping the home page visual language unchanged: light gray page background, white 8rpx panels, thin borders, restrained typography, and teal primary accents.

## Current Context

The existing `pages/profile` page already handles login, privacy authorization, avatar upload, nickname/profile editing, collection navigation, history navigation, logout, and share metadata. The change should preserve this behavior and reorganize the WXML/WXSS around a richer personal-center layout.

## Recommended Approach

Use a dashboard layout with four vertical sections:

1. User overview panel
   - Avatar, nickname, role/login status, and short profile summary.
   - Show the existing profile-incomplete warning inside this panel when needed.
   - Keep the styling close to article cards: white panel, 8rpx radius, `#e5e7eb` border, teal highlights.

2. Status summary panel
   - Show three compact summary cells using only trustworthy local state for now.
   - Suggested cells: `收藏`, `历史`, `资料状态`.
   - Avoid fake numeric counts until a real summary API exists.

3. Feature grid panel
   - Use a two-column grid for real, currently supported actions.
   - Initial actions: `我的收藏`, `浏览历史`, `编辑资料`, `隐私协议`.
   - Each item should have a short title and muted description, with teal used as the active cue.

4. Profile settings and account panel
   - Keep avatar selection, nickname input, profile textarea, and save button available.
   - Keep account details (`用户 ID`, `角色`) and the red outline logout button.

## Data Flow

No backend change is required for the first implementation.

- Continue loading user data with `auth.ensureLogin()` in `loadUser()`.
- Continue deriving profile completeness with `auth.isProfileIncomplete(user)`.
- Continue routing collections and history with existing `openCollections()` and `openHistory()`.
- Add a local `profileStatusText` or equivalent derived display state if helpful.

Future optional API:

```text
GET /mini/api/user/summary
```

Potential response:

```json
{
  "collectionCount": 12,
  "historyCount": 38,
  "praiseCount": 5,
  "commentCount": 3,
  "messageUnreadCount": 0
}
```

Do not add this API call in the current pass unless the backend already provides it.

## States

- Loading: keep the existing centered state card.
- Logged out: keep a compact login prompt card using the shared `state-card` and `primary-button` styles.
- Privacy authorization needed: keep the existing authorization state and privacy contract action.
- Logged in: show the dashboard layout.
- Uploading/saving/logging out: preserve disabled and loading states.

## Testing

Manual verification should cover:

- Logged-out profile page.
- Privacy authorization prompt.
- Logged-in dashboard layout.
- Avatar selection disabled/loading state.
- Nickname/profile validation and save.
- Navigation to collection and history pages.
- Logout state reset.

## Scope Boundaries

- Do not change the home page style or behavior.
- Do not introduce fake statistics.
- Do not add empty feature pages.
- Do not refactor unrelated collection/history/index code.
