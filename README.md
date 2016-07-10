# FeedExpander
Feed expander is a service which provides a generic way to create rss or atom feeds containing a specific part of the linked page. In other words, with FeedExpander you can convert a cropped feed into a full feed.  

## Build
You need to have git, maven and java 1.8 installed before doing the following.
  * `cd /opt/`
  * `git clone https://github.com/meerkatzenwildschein/FeedExpander.git` 
  * `cd FeedExpander/` 
  * `mvn install` 

## Run
 * Edit the config.yml and change the htuser and whitelist.txt. 
 * Start the service with `java -jar target/expander-x.x.x.jar server config.yml`
 
## Update
  * Copy away you config.yml
  * `cd /opt/FeedExpander`
  * `git fetch --all`
  * `git reset --hard origin/master`
  * `mvn install`
  * Look for changes between you old and the updated config.yml

## Security hints
  If you plan to use the FeedExpander service as some private service you have to configure the htusers files where you can simply add some users and their password which are allowed to get access. It would be also be much more safe to use https instead of the preconfigured http setup.
  
  In the case that FeedExpander is running on the same machine as the feed reader you are using (for example TTRSS) it would be a good idea configure FeedExpander with `bindHost: 127.0.0.1` which is already the default configuration. 
It is generally a good idea to reduce the visibility of a service using a firewall or defining some iptable rules.  
  
## Usage
  Before the FeedExpander can be used, the feeds that should be expanded have to be configured in the `feed-sites.config` file. The alias which is configured in the `feed-sites.config` file can be used to load the expanded feed. The default configured url is `http://localhost:9998/expand?alias=abc`. The alias `abc` have to be replaced with this one specified in the config file. 
  
  The `selector` in the `feed-sites.config` file selects a part of a html page. It describes a navigation path through the html elements using a CSS (or jquery) like selector syntax.
  
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
