# User Module Refactoring - Centralized API & Context Management

## Overview
This refactoring implements a centralized API service and React Context to manage user data globally, eliminating redundant API calls and preventing unnecessary page reloads during navigation.

## What Was Changed

### 1. **New Service Layer** (`src/services/userService.js`)
All user-related API calls are now centralized in a single service file:
- `getMe()` - Fetch current user profile
- `getTransactions()` - Fetch user transactions
- `getOffers()` - Fetch available offers
- `getRedemptions()` - Fetch user redemptions
- `claimPoints()` - Claim activity points
- `redeemOffer()` - Redeem an offer

**Benefits:**
- Single source of truth for API endpoints
- Easier to maintain and update
- Better error handling
- Consistent response structure

### 2. **User Context Provider** (`src/context/UserContext.jsx`)
Global state management for user data:
- Stores user, transactions, offers, and redemptions
- Provides methods to fetch and update data
- Prevents redundant API calls by caching data
- Automatic data initialization on app load

**Context Methods:**
- `user` - Current user object
- `transactions` - User transaction list
- `offers` - Available offers list
- `redemptions` - User redemption history
- `loading` - Loading state
- `claimPoints(activityCode, points, note)` - Claim points with auto-refresh
- `redeemOffer(offerId, store)` - Redeem offer with auto-refresh
- `refreshAll()` - Manually refresh all data

### 3. **Updated Components**

#### Dashboard.jsx
- **Before:** Made separate API calls for user and transactions on mount
- **After:** Uses `useUser()` hook to access cached data
- **Benefits:** No API calls if data already loaded, instant navigation

#### Offers.jsx
- **Before:** Fetched offers and user data on every mount
- **After:** Uses context for both offers and user data
- **Benefits:** Offers load instantly when navigating back

#### Profile.jsx
- **Before:** Fetched user data individually
- **After:** Uses shared user context
- **Benefits:** Profile data available immediately

#### Redemptions.jsx
- **Before:** Fetched redemptions on mount
- **After:** Uses context redemptions
- **Benefits:** Auto-updates after redeeming an offer

#### Transactions.jsx
- **Before:** Fetched transactions and user data separately
- **After:** Uses context for both
- **Benefits:** Auto-updates after claiming points

### 4. **Updated App.jsx**
Wrapped the entire app with `UserProvider` to make context available globally.

### 5. **Updated Login.jsx**
After successful login, triggers `refreshAll()` to load user data immediately.

### 6. **Updated Header.jsx**
On logout, clears user context along with localStorage.

## How to Use

### In Any Component
```jsx
import { useUser } from '../../context/UserContext'

function MyComponent() {
  const { user, transactions, loading, claimPoints } = useUser()
  
  if (loading) return <div>Loading...</div>
  
  return (
    <div>
      <h2>Welcome {user?.name}</h2>
      <p>Points: {user?.profile?.pointsBalance}</p>
    </div>
  )
}
```

### Claim Points Example
```jsx
const { claimPoints } = useUser()

const handleClaim = async () => {
  await claimPoints('LOGIN', 50, 'Daily Login Bonus')
  // User and transactions are automatically refreshed
}
```

### Redeem Offer Example
```jsx
const { redeemOffer } = useUser()

const handleRedeem = async (offerId) => {
  await redeemOffer(offerId, 'Online')
  // User, transactions, and redemptions are automatically refreshed
}
```

## Navigation Without Reload

All navigation now uses React Router's `Link` component instead of `<a>` tags:
```jsx
import { Link } from 'react-router-dom'

<Link to="/user/profile">Go to Profile</Link>
```

This ensures:
- ✅ No page reloads
- ✅ Instant navigation
- ✅ Preserved context data
- ✅ Better user experience

## Performance Improvements

### Before Refactoring:
- Dashboard: 2 API calls (user + transactions)
- Offers: 2 API calls (offers + user)
- Profile: 1 API call (user)
- Redemptions: 1 API call (redemptions)
- Transactions: 2 API calls (transactions + user)

**Total on full navigation:** 8+ API calls

### After Refactoring:
- Initial load: 4 API calls (user, transactions, offers, redemptions)
- Navigation between pages: **0 API calls** (uses cached data)
- Only refresh when needed (claim, redeem, manual refresh)

**Total on full navigation:** 4 API calls (75% reduction!)

## Key Benefits

1. **No More Page Reloads** - React Router handles all navigation client-side
2. **Centralized API Management** - Single file for all user API calls
3. **Reduced API Calls** - Context caching eliminates redundant requests
4. **Better User Experience** - Instant page navigation, no flickering
5. **Automatic Updates** - Context auto-refreshes after mutations (claim, redeem)
6. **Easier Maintenance** - One place to update API logic
7. **Type Safety Ready** - Easy to add TypeScript in the future
8. **Error Handling** - Centralized error management

## Files Structure

```
frontend/src/
├── context/
│   └── UserContext.jsx          # Global user state management
├── services/
│   └── userService.js           # Centralized API calls
├── pages/user/
│   ├── Dashboard.jsx            # Updated to use context
│   ├── Offers.jsx               # Updated to use context
│   ├── Profile.jsx              # Updated to use context
│   ├── Redemptions.jsx          # Updated to use context
│   └── Transactions.jsx         # Updated to use context
├── pages/auth/
│   └── Login.jsx                # Triggers context refresh on login
├── components/
│   └── Header.jsx               # Clears context on logout
└── App.jsx                      # Wrapped with UserProvider
```

## Testing the Changes

1. **Login** - User data loads immediately
2. **Navigate to Offers** - Instant load, no API call
3. **Navigate to Profile** - Instant load, uses cached user data
4. **Navigate to Transactions** - Instant load, uses cached data
5. **Claim Points on Dashboard** - User balance and transactions auto-update
6. **Redeem Offer** - Balance, transactions, and redemptions auto-update
7. **Logout** - Context cleared, ready for next login

## Future Enhancements

1. Add React Query for advanced caching and background updates
2. Implement optimistic UI updates
3. Add WebSocket support for real-time updates
4. Implement data persistence with sessionStorage
5. Add TypeScript for type safety
6. Add loading skeletons for better UX
7. Implement error boundaries for graceful error handling

## Troubleshooting

### Context not available
Make sure the component is inside `UserProvider` in App.jsx

### Data not refreshing
Call `refreshAll()` manually or use specific refresh methods

### Logout not clearing data
Ensure Header component properly calls `setUser(null)`

---

**Created by:** Refactoring Initiative
**Date:** February 6, 2026
**Version:** 1.0.0
