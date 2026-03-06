# California State Web Template - Color Themes

All color themes from the California State Web Template are available in this project.

## Available Color Themes

The following color themes are included and ready to use:

1. **Delta** - A blue-gray, a medium-light blue-gray, a light blue-gray, and a dark blue-gray
2. **Eureka** - A dark gray, a beige, an off-white, and an off-black
3. **Mono** - A dark gray, a yellow, a light gray, and an off-black
4. **Oceanside** - A medium blue, a yellow, a light blue, and a dark gray
5. **Orange County** - A dark orange, a yellow-orange, a cream, and a dark brown
6. **Paso Robles** - A dark red, a yellow-orange, a light gray, and a dark brown
7. **Sacramento** (DEFAULT) - A dark blue, a medium-light blue, a light blue, and a dark red
8. **Santa Barbara** - A brown, a medium-light orange, a cream, and a dark brown
9. **Santa Cruz** - A blue, an orange, a light blue, and a dark blue
10. **Shasta** - A green, a yellow, a light green, and a dark gray
11. **Sierra** - A green, a yellow, a light green, and a dark green
12. **Trinity** - A blue-gray, a beige, a light gray, and an off-black

## How to Change Color Theme

To change the color theme, edit `/frontend/src/index.js`:

```javascript
// Change this line to use a different color theme
import "@cagovweb/state-template/dist/css/colortheme-sacramento.css";
```

Available theme files:
- `colortheme-delta.css`
- `colortheme-eureka.css`
- `colortheme-mono.css`
- `colortheme-oceanside.css`
- `colortheme-orangecounty.css`
- `colortheme-pasorobles.css`
- `colortheme-sacramento.css` (Current Default)
- `colortheme-santabarbara.css`
- `colortheme-santacruz.css`
- `colortheme-shasta.css`
- `colortheme-sierra.css`
- `colortheme-trinity.css`

## Sacramento Color Palette (Current Theme)

### Primary Colors

- **Primary** (#153554) - Dark blue
  - Class: `.color-primary`
  - Variable: `--color-p2`
  - RGB: 21, 53, 84

- **Highlight** (#7BB0DA) - Medium-light blue
  - Class: `.color-highlight`
  - Variable: `--color-p1`
  - RGB: 123, 176, 218

- **Standout** (#343B4B) - Dark gray-blue
  - Class: `.color-standout`
  - Variable: `--color-p3`
  - RGB: 52, 59, 75

### Secondary Colors

- **Secondary 1** (#F5F5F5) - Light gray
  - Class: `.color-s1`
  - Variable: `--color-s1`
  - RGB: 245, 245, 245

- **Secondary 2** (#CCCCCC) - Medium gray
  - Class: `.color-s2`
  - Variable: `--color-s2`
  - RGB: 204, 204, 204

- **Secondary 3** (#e6dcdc) - Off-white with slight pink
  - Class: `.color-s3`
  - Variable: `--color-s3`
  - RGB: 230, 220, 220

### Grayscale Palette

Works with all color themes:
- `.color-white` / `.bg-white` - #ffffff
- `.gray-50` / `.bg-gray-50` - #fafafa
- `.gray-100` / `.bg-gray-100` - #ededef
- `.gray-200` / `.bg-gray-200` - #d4d4d7
- `.gray-300` / `.bg-gray-300` - #bcbbc1
- `.gray-400` / `.bg-gray-400` - #a4a3ab
- `.gray-500` / `.bg-gray-500` - #898891
- `.gray-600` / `.bg-gray-600` - #72717c
- `.gray-700` / `.bg-gray-700` - #5e5e6a
- `.gray-800` / `.bg-gray-800` - #4a4958
- `.gray-900` / `.bg-gray-900` - #3b3a48
- `.color-black` / `.bg-black` - #000000

## Using Colors in Your Components

### CSS Classes

```html
<!-- Text colors -->
<p class="color-primary">Primary color text</p>
<p class="color-highlight">Highlight color text</p>
<p class="color-standout">Standout color text</p>

<!-- Background colors -->
<div class="bg-primary">Primary background</div>
<div class="bg-highlight">Highlight background</div>
<div class="bg-gray-50">Light gray background</div>
```

### CSS Variables

```css
.custom-element {
  color: var(--color-p2); /* Primary */
  background-color: var(--color-p1); /* Highlight */
  border-color: var(--color-p3); /* Standout */
}
```

## Accessibility

All color themes follow accessibility best practices with sufficient contrast ratios between text and backgrounds. Each theme has CSS rules to ensure proper color contrast for people with low vision.

## References

- [Official Color Theme Documentation](https://template.webstandards.ca.gov/visual-design/color.html)
- [Sacramento Theme](https://template.webstandards.ca.gov/visual-design/color/?sacramento)
- [State Web Template GitHub](https://github.com/Office-of-Digital-Services/California-State-Web-Template-HTML)
