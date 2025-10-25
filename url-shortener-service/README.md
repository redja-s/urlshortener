# URL Shortener Service

## Description

### Responsibilities
1. Generate a short URL from a long URL
2. Validate long URLs
3. Store mappings between long URL and generated short URL

### Endpoints
This service contains the following endpoints:

```markdown
POST /api/shorten

Request: { "url": "https://example.com/very/long/url" }
Response: { "shortUrl": "https://short.ly/abc123", "shortCode": "abc123" , "createdAt": "..." }
```

```markdown
GET /api/urls/:shortcode

Response: { "shortCode": "abc123", "longUrl": "https://...", "createdAt": "...", "clicks": 1234 }
```

```markdown
DELETE /api/urls/:shortcode

Response: { "success": true }
```

### Database Design

```sql
```sql
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    INDEX idx_short_code (short_code)
);
```