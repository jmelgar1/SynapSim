# SynapSim Design Guide

**Purpose:** Quick reference for maintaining visual consistency in future development

---

## Design Principles

**Theme:** Modern, neuroscience-inspired aesthetic balancing scientific credibility with approachability

**Core Values:**
- Clean layouts with ample whitespace
- Smooth, purposeful animations that provide feedback
- Educational focus without overwhelming users
- Professional presentation for medical/scientific context

---

## Color System

### Primary Brand Colors

**Gradient Identity:**
```css
linear-gradient(135deg, #667eea 0%, #764ba2 100%)
```
Use for: Primary buttons, headings, progress bars, highlights

**Semantic Variables (use these, not hardcoded colors):**
```css
--color-background       /* Main backgrounds */
--color-background-soft  /* Secondary backgrounds */
--color-border          /* Standard borders */
--color-border-hover    /* Hover borders */
--color-heading         /* Headings */
--color-text            /* Body text */
```

### Accent Colors
- **Success/Completed**: `#4ade80` (green)
- **Warning/Intermediate**: `#fbbf24` (yellow)
- **Error/Advanced**: `#e74c3c` (red)
- **Link**: `hsla(160, 100%, 37%, 1)` (teal)

### Page Backgrounds
```css
/* Standard page background */
background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);

/* Hero/emphasis areas */
background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
```

---

## Typography

### Font Stack
```css
font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
```

### Size Scale
- **Hero Title**: `4rem` (64px), weight 800
- **Page Title**: `2.5rem` (40px), weight 800
- **Section Title**: `1.75rem` (28px), weight 700
- **Card Title**: `1.25rem` (20px), weight 700
- **Body**: `1rem` (16px), weight 400, line-height 1.6
- **Small/Caption**: `0.85rem` (13.6px), weight 500-600

### Special Styles

**Gradient Headings:**
```css
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
-webkit-background-clip: text;
-webkit-text-fill-color: transparent;
background-clip: text;
```

**Uppercase Labels:**
```css
text-transform: uppercase;
letter-spacing: 0.5px;
font-weight: 600;
font-size: 0.85rem;
```

---

## Spacing & Layout

### Spacing Scale
Use consistent increments: `0.5rem`, `1rem`, `1.5rem`, `2rem`, `2.5rem`, `3rem`

### Border Radius
- **Small**: `8px` (buttons, inputs)
- **Medium**: `12px` (cards)
- **Large**: `16px` (major sections, modals)

### Shadows
```css
/* Default cards */
box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

/* Elevated cards/modals */
box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);

/* Primary buttons */
box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
```

### Max Widths
- **Content**: `1200px`
- **Forms**: `900px`
- **Modals**: `650px`

---

## Component Patterns

### Primary Button
```css
padding: 12px 32px;
font-size: 1.1rem;
font-weight: 600;
border-radius: 8px;
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
color: white;
text-transform: uppercase;
letter-spacing: 0.5px;
transition: all 0.3s ease;
box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);

/* Hover: lift up 2px, enhance shadow */
/* Active: return to baseline, reduce shadow */
/* Disabled: opacity 0.5, no cursor */
```

### Cards
```css
background: var(--color-background);
border: 2px solid var(--color-border);
border-radius: 16px;
padding: 2rem;
transition: all 0.3s ease;

/* Hover */
border-color: #667eea;
transform: translateY(-4px);
box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
```

### Modals
```css
/* Overlay */
background: rgba(0, 0, 0, 0.6);
backdrop-filter: blur(4px);

/* Content */
background: var(--color-background);
border-radius: 16px;
box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
animation: modalSlideIn 0.3s ease-out;
```

### Form Inputs
```css
padding: 1rem;
border: 2px solid var(--color-border);
border-radius: 8px;
transition: all 0.2s ease;

/* Focus/hover */
border-color: #667eea;
outline: none;
```

---

## Animations

### Standard Transitions
```css
transition: all 0.2s ease;    /* Fast (buttons, hover) */
transition: all 0.3s ease;    /* Medium (cards, modals) */
transition: width 0.6s ease;  /* Slow (progress bars) */
```

### Common Animations
```css
/* Fade In */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Slide Up (modals, cards) */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Fade In Down (headings) */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### Hover Effects
- **Lift**: `translateY(-4px)` for cards, `translateY(-2px)` for buttons
- **Slide**: `translateX(4px)` for forward actions, `translateX(-4px)` for back
- **Scale**: `scale(1.1)` for active states

---

## Responsive Design

### Breakpoint
```css
@media (max-width: 768px) {
  /* Mobile/tablet adjustments */
}
```

### Mobile Patterns
- Reduce font sizes (hero: 4rem → 2.5rem)
- Reduce padding (2rem → 1.5rem)
- Single column grids
- Stack flex containers vertically
- Full-width buttons

---

## Dark Mode

Auto-detects via system preference:
```css
@media (prefers-color-scheme: dark) {
  :root {
    --color-background: #181818;
    --color-background-soft: #222222;
    --color-heading: #ffffff;
    --color-text: rgba(235, 235, 235, 0.64);
    /* etc */
  }
}
```

**Always use CSS variables** for colors to ensure dark mode compatibility.

---

## Accessibility

### Required for All Components
- **Focus States**: Visible outline on keyboard focus
  ```css
  outline: 2px solid #667eea;
  outline-offset: 2px;
  ```
- **Color Contrast**: WCAG AA minimum (4.5:1 for text)
- **Keyboard Navigation**: All interactive elements focusable
- **Semantic HTML**: Proper heading hierarchy, button elements

### Motion Preferences
```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## Development Checklist

When building new features:

- [ ] Use CSS variables for all colors
- [ ] Follow spacing scale (0.5rem increments)
- [ ] Include hover/active/focus states
- [ ] Use standard border radius (8px, 12px, 16px)
- [ ] Add smooth transitions (0.2s-0.3s)
- [ ] Test at 768px breakpoint
- [ ] Verify dark mode appearance
- [ ] Ensure keyboard accessibility
- [ ] Meet WCAG AA contrast standards

---

## Quick Reference

**Primary Gradient**: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`

**Standard Transition**: `transition: all 0.3s ease`

**Card Style**: `border-radius: 16px`, `padding: 2rem`, `border: 2px solid var(--color-border)`

**Button Style**: `border-radius: 8px`, `padding: 12px 32px`, gradient background

**Hover Lift**: `transform: translateY(-4px)`, enhanced shadow

**Mobile Breakpoint**: `768px`

**Max Content Width**: `1200px`

---

**Key Files:**
- Base styles: `src/assets/base.css`
- Common components: `src/components/common/`
- Example patterns: See `HomeView.vue`, `Dashboard.vue`, `ScenarioBuilder.vue`
