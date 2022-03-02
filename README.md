# aql-idea

## Plugin description

<!-- Plugin description -->

AQL support for Idea.

<!-- Plugin description end -->

## TODO
* Data Sources (project/global?)
* Execution log history (query/response)
* Autocomplete
* Publish/move to better-care github?
* Tree view

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "aql-idea"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/markopi/aql-idea/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

### Define ehr servers

- Open AQL tool window. 
- In Servers tab, click Configure button. 
- Add one or more servers by clicking + button and configure server properties. 
  Test button will test the configuration by making a test request to the current server
- Click Ok to save the server configuration

Servers will now be listed in the Servers tab inside the AQL tool window. To choose the currently active server, double-click on it. 
Currently active server has an icon next to it.

### Aql editing

In the project, create a file with an .aql extension. This file should support: 
- syntax highlighting 
- autocompletion of keywords 
- autocompletion of paths and archetypeIds according to the currently active server metadata 
- executing an aql on the currently active server
 
### Executing an aql

In the gutter there should be a Run icon next to each start of an aql. Click on it to run that particular aql on the
currently active server. The results will be shown in AQL tool window, tab Query Results. Besides the results as a table,
there will also be tabs that contain the raw request and response contents.


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
