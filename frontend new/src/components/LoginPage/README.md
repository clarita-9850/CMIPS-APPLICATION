# CMIPS Login Page Component

A professional, accessible login page for the California Case Management Information and Payrolling System (CMIPS), built with the California State Web Template React components.

## About CMIPS

CMIPS is the Case Management Information and Payrolling System that supports California's In-Home Supportive Services (IHSS) Program. The system:

- Manages over 815,000 active recipients
- Supports over 716,000 eligible providers
- Processes over 1.65 million timesheets per month
- Handles over $19 billion per year in payroll payments
- Serves 6,800 end users across all 58 California counties

## Features

✅ **Fully Accessible** - WCAG 2.1 AA compliant with proper ARIA labels
✅ **California State Branding** - Uses Sacramento color theme
✅ **Form Validation** - Client-side validation with clear error messages
✅ **Responsive Design** - Mobile-friendly and works on all devices
✅ **Loading States** - Visual feedback during authentication
✅ **Help Section** - Built-in user guidance and support information
✅ **Security Best Practices** - Proper form handling and autocomplete attributes

## Usage

### Basic Implementation

```javascript
import { LoginPage } from './components/LoginPage';

function App() {
  const handleLogin = (credentials) => {
    console.log('Login attempt:', credentials);
    // Your authentication logic here
  };

  return (
    <LoginPage
      onLogin={handleLogin}
      isLoading={false}
      errorMessage=""
    />
  );
}
```

### With Authentication State

```javascript
import React, { useState } from 'react';
import { LoginPage } from './components/LoginPage';

function App() {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleLogin = async (credentials) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      const response = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials)
      });

      if (response.ok) {
        const data = await response.json();
        // Store token and redirect
        localStorage.setItem('token', data.token);
        window.location.href = '/dashboard';
      } else {
        setErrorMessage('Invalid username or password');
      }
    } catch (error) {
      setErrorMessage('An error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <LoginPage
      onLogin={handleLogin}
      isLoading={isLoading}
      errorMessage={errorMessage}
    />
  );
}
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `onLogin` | function | - | Callback function called with `{username, password, rememberMe}` when form is submitted |
| `isLoading` | boolean | `false` | Shows loading spinner and disables form during authentication |
| `errorMessage` | string | `''` | Error message to display above the form |

## Testing the Demo

To test the login page with the included example:

1. **Enable Login View** - In `App.js`, change `showLogin` to `true`:
   ```javascript
   const [showLogin, setShowLogin] = React.useState(true);
   ```

2. **Demo Credentials**:
   - Username: `demo`
   - Password: `demo`

3. **Restart the application** to see the login page

## Styling

The component uses the Sacramento color theme by default. The main colors are:

- **Primary** (#153554) - Dark blue for header background
- **Highlight** (#7BB0DA) - Medium-light blue for focus states
- **Standout** (#343B4B) - Dark gray-blue for text

To customize colors, edit `LoginPage.css` or use CSS variables:

```css
.login-page {
  background: linear-gradient(135deg, var(--color-p2) 0%, var(--color-p2-dark) 100%);
}
```

## Integration with Backend

### Spring Boot Example

```java
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Your authentication logic
        if (authService.authenticate(request.getUsername(), request.getPassword())) {
            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Invalid credentials"));
    }
}
```

### Frontend Integration

Update `LoginPageExample.js` to call your backend:

```javascript
const handleLogin = async (credentials) => {
  setIsLoading(true);
  setErrorMessage('');

  try {
    const response = await fetch('http://localhost:8080/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: credentials.username,
        password: credentials.password
      })
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('authToken', data.token);
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } else {
      setErrorMessage('Invalid username or password');
    }
  } catch (error) {
    setErrorMessage('Connection error. Please try again.');
  } finally {
    setIsLoading(false);
  }
};
```

## Accessibility Features

- ✓ Semantic HTML structure
- ✓ Proper form labels and ARIA attributes
- ✓ Keyboard navigation support
- ✓ Screen reader friendly error messages
- ✓ Focus management
- ✓ Color contrast compliance (WCAG AA)
- ✓ Reduced motion support

## Files

- `LoginPage.js` - Main login component
- `LoginPage.css` - Component styles
- `LoginPageExample.js` - Example implementation with authentication
- `index.js` - Export file

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Android)

## Related Links

- [CMIPS Official Website](https://www.cmips.osi.ca.gov/)
- [CA State Web Template](https://template.webstandards.ca.gov/)
- [CalHHS OTSI](https://www.osi.ca.gov/)
