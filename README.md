# aql-idea

## Plugin description

<!-- Plugin description -->

AQL support for Idea.

<!-- Plugin description end -->

## TODO
* Data Sources (global)
* Script files under DataSource
* Publish/move to better-care github?
* Tree view/table: http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm

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

Servers will now be listed in the Servers tab inside the AQL tool window. To open a console for a particular server, 
double click on the server name 

### Aql editing

The aql console supports: 
- syntax highlighting 
- autocompletion of keywords 
- autocompletion of paths and archetypeIds according to the currently active server metadata 
- executing an aql on the currently active server
- multiple AQLs per file, which can be run individually
 
### Executing an aql

In the gutter there should be a Run icon next to each start of an aql. Click on it to run that particular aql on the
currently active server. The results will be shown in AQL tool window, tab Query Results. Besides the results as a table,
there will also be tabs that contain the raw request and response contents.


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
