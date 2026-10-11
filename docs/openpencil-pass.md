# OpenPencil pass — pressing-dz

## Design (Shared/OpenPencil/pressing-dz/)
- `design.html` (ticket card + status summary + add action, DZ tokens) → `design.fig`
- `lint --preset accessibility`: 0 errors, 0 warnings

## Code review (no rewrite needed)
Add/delete icons already use string-resource descriptions (`new_ticket_cd`, `action_delete_cd`).
No code change.

## APKs
`ci.yml` adds `assembleRelease` (unsigned) + `pressingdz-release` artifact + a versioned
`release-v*-b*` GitHub Release for the release APK (debug flow unchanged).
