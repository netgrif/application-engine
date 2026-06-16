# Menu Item Template Creation Guide

## Overview

Templates provide predefined configurations for menu items that can be reused throughout the application. Each template
encapsulates a complete menu item structure with its associated views and filters.

## Template Structure

A template must implement the `Template` interface with three core components:

- **Identifier**: A unique string constant that identifies the template
- **Name**: An internationalized string (I18nString) providing the display name in multiple languages
- **Template Body**: A MenuItemBody instance containing the complete menu item configuration

## Creation Steps

1. **Create Template Class**
    - Implement the `Template` interface
    - Define a unique public static IDENTIFIER constant
    - Create a private static I18nString NAME with translations
    - Declare a private static MenuItemBody TEMPLATE field

2. **Build Template Configuration**
    - Implement a private static `buildTemplate()` method
    - Instantiate MenuItemBody and configure its properties
    - Create and configure the appropriate view body (CaseViewBody, TaskViewBody, ...)
    - Set up FilterBody for data filtering requirements
    - Configure chained views if needed (e.g., TaskViewBody within CaseViewBody)
    - Wire all components together through setter methods

3. **Implement Interface Methods**
    - Return IDENTIFIER from `getIdentifier()`
    - Return NAME from `getName()`
    - Return TEMPLATE from `getTemplate()`

4. **Register Template**
    - Add the template to the `MenuItemTemplateHolder.templates` map
    - Use the template's IDENTIFIER as the key
    - Instantiate the template class as the value

## Best Practices

- Keep identifiers lowercase with underscores for consistency
- Provide translations for all supported languages in the NAME field
- Build template configuration statically to ensure immutability
- Ensure all required view and filter configurations are properly initialized
- Use descriptive identifiers that reflect the template's purpose
