# Professional Email Templates for User Service

## Overview
This implementation includes professional HTML and plain text email templates for OTP verification and password reset functionality.

## Templates Included

### 1. OTP Verification Email
- **HTML Template**: `src/main/resources/templates/otp-email-template.html`
- **Plain Text Template**: `src/main/resources/templates/otp-email-plain.txt`
- **Usage**: Login verification, account verification

### 2. Password Reset Email
- **HTML Template**: `src/main/resources/templates/password-reset-email-template.html`
- **Plain Text Template**: Generated dynamically in `EmailService`
- **Usage**: Password reset requests

## Features

### 🎨 Professional Design
- Modern, responsive HTML design
- Gradient headers with brand colors
- Clean typography using system fonts
- Mobile-friendly layout

### 🔒 Security Focused
- Clear security warnings and tips
- OTP expiration notifications
- Anti-phishing messaging
- Professional branding

### 📱 Multi-format Support
- HTML version for modern email clients
- Plain text fallback for all clients
- Automatic template selection based on client capabilities

### 🎯 User Experience
- Clear call-to-action buttons
- Step-by-step instructions
- Visual hierarchy with icons and colors
- Professional footer with contact information

## Template Variables

### OTP Templates
- `{{OTP_CODE}}` - The 6-digit verification code

### Password Reset Templates
- `{{OTP_CODE}}` - The 6-digit password reset code

## Email Service Methods

### `sendOtpEmail(String to, String otp)`
- Sends OTP verification email
- Uses both HTML and plain text templates
- Subject: "🔐 Your OTP for CyberLearnix - Secure Verification"

### `sendPasswordResetEmail(String to, String otp)`
- Sends password reset email
- Uses password reset specific templates
- Subject: "🔒 Password Reset Request - CyberLearnix"

## Branding Elements

### Colors
- **Primary**: #667eea (Blue gradient)
- **Secondary**: #e74c3c (Red for password reset)
- **Success**: #28a745 (Green)
- **Warning**: #ffc107 (Yellow)
- **Danger**: #dc3545 (Red)

### Typography
- **Headers**: Segoe UI, 28px, bold
- **Body**: Segoe UI, 16px, regular
- **Code**: Courier New, 36px, bold (for OTP)

### Icons
- 🔐 OTP Verification
- 🔒 Password Reset
- ⏰ Expiration warnings
- 🛡️ Security tips
- ⚠️ Important notices

## Security Features

### Anti-Phishing
- Clear sender identification
- Professional branding consistency
- Security warnings about sharing codes
- Contact information for verification

### User Education
- Password strength requirements
- Security best practices
- Step-by-step instructions
- Clear expiration times

## Testing

### Manual Testing
1. Send OTP to test email
2. Verify HTML rendering in email client
3. Check plain text fallback
4. Test on mobile devices

### Email Client Compatibility
- Gmail (Web, Mobile, Desktop)
- Outlook (Web, Desktop, Mobile)
- Apple Mail
- Thunderbird
- Other IMAP/POP3 clients

## Customization

### Brand Colors
Update CSS variables in HTML templates:
```css
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
```

### Company Information
Update footer section in all templates:
```html
<p><strong>Your Company Name</strong></p>
<p>📧 support@yourcompany.com</p>
```

### Email Addresses
Update sender email in `EmailService.java`:
```java
helper.setFrom("your-email@yourcompany.com");
```

## File Structure
```
src/main/resources/templates/
├── otp-email-template.html          # HTML OTP template
├── otp-email-plain.txt              # Plain text OTP template
└── password-reset-email-template.html # HTML password reset template
```

## Dependencies
- Spring Boot Mail Starter
- JavaMail API
- Spring Core (for ClassPathResource)

## Usage Example

```java
@Autowired
private EmailService emailService;

// Send OTP
emailService.sendOtpEmail("user@example.com", "123456");

// Send password reset
emailService.sendPasswordResetEmail("user@example.com", "789012");
```

## Best Practices

1. **Always test** email templates in multiple clients
2. **Keep templates updated** with current branding
3. **Monitor delivery rates** and spam scores
4. **Use professional sender addresses**
5. **Include unsubscribe options** for marketing emails
6. **Test with real email addresses** before production

## Troubleshooting

### Common Issues
1. **Templates not loading**: Check file paths in `ClassPathResource`
2. **HTML not rendering**: Verify MIME type settings
3. **Images not showing**: Use absolute URLs for images
4. **Spam filters**: Test with different email providers

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.com.userservice.userservice.service.EmailService=DEBUG
```
