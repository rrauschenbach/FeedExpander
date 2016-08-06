# FeedExpander
Feed expander is a java service which provides a generic way to create rss or atom feeds containing a specific part of the linked page. In other words, with FeedExpander you can convert a cropped feed into a full feed. Additionally there is also a filter functions which allows to filter feeds using regular expressions.  

## Build
You need to have git, maven and java 1.8 installed before doing the following.
  * `cd /opt/`
  * `git clone https://github.com/meerkatzenwildschein/FeedExpander.git` 
  * `cd FeedExpander/` 
  * `mvn install` 

## Run
 * Edit the `config.yml` and change it to your needs. 
 * Start the service with `java -jar target/expander-x.x.x.jar server config.yml`
 
## Update
  * Stop feedexpander if running.
  * `cd /opt/FeedExpander`
  * Copy away your `config.yml` and `feed-config.yml`.
  * `git fetch --all`
  * `git reset --hard origin/master`
  * `mvn install`
  * Look for changes between your backuped and the updated config files.

## Security hints
  If you plan to use the FeedExpander service as some private service you may configure the htusers files where you can simply add some users and their password which are allowed to get access. Feed readers must have simple http auth support for using this. It would be also be much more safe to use https instead of the preconfigured http setup.
  
  In the case that FeedExpander is running on the same machine as the feed reader you are using (for example TTRSS) it would be a good idea configure FeedExpander with `bindHost: 127.0.0.1` which is already the default configuration.
  
## Usage
  Before the FeedExpander can be used, the feeds that should be expanded have to be configured in the `feed-config.yml` file. The alias which is configured in the `feed-config.yml` file is used as a part of the expander's feed url. The default configured url is `http://localhost:9998/expand?alias=abc`. The alias `abc` have to be replaced with this one specified in the config file. All configured, expanded url's can be listed with the url `http://localhost:9998/feeds`.
  
### Expand feeds 
  The `selector` in the `feed-config.yml` file selects a part of a html page. It describes a navigation path through the html elements using a CSS (or jquery) like selector syntax. The selected part will be shown as article content in the result feed.
  
### Filter feeds
  The `includeFilter` and `excludeFilter` in the `feed-config.yml` allows to configure regular expressions (or just simple keywords) to filter feed articles. If the `includeFilter` matches to a feed article, it will get removed / filtered. On the other hand, the `excludeFilter` allow to specify an expression which removes all other, not matching articles from the feed.
  
### Selector overview
  - `tagname`: find elements by tag, e.g. `a`
  - `ns|tag`: find elements by tag in a namespace, e.g. `fb|name finds <fb:name> elements`
  - `#id`: find elements by ID, e.g. `#logo`
  - `.class`: find elements by class name, e.g. `.masthead`
  - `[attribute]`: elements with attribute, e.g. `[href]`
  - `[^attr]`: elements with an attribute name prefix, e.g. `[^data-]` finds elements with HTML5 dataset attributes
  - `[attr=value]`: elements with attribute value, e.g. `[width=500]` (also quotable, like [data-name='launch sequence'])
  - `[attr^=value], [attr$=value], [attr*=value]`: elements with attributes that start with, end with, or contain the value, e.g. `[href*=/path/]`
  - `[attr~=regex]`: elements with attribute values that match the regular expression; e.g. `img[src~=(?i)\.(png|jpe?g)]`
  - `*`: all elements, e.g. `*`
  
  See [Selector syntax](https://jsoup.org/apidocs/org/jsoup/select/Selector.html) for more details.
