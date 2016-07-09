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
  * `cd /opt/FeedExpander`
  * `git pull`
  * `mvn install`

## Security hints
  The client have the possibility to make the FeedExpander service to load a feed and the linked html pages from some self defined url. This can for example be misused to run DDOS attacks or to load some unexpected things over this service. It`s highly recommended to use the white list functionality to prohibit those cases.
  
  If you plan to use the FeedExpander service as some private service you have to configure the htusers files where you can simply add some users and their password which are allowed to get access. It would also be much more safe to use https instead of the preconfigured http setup.
  
  In the case that FeedExpander is running on the same machine as the feed reader you are using (for example TTRSS) it would be a good idea configure FeedExpander with `bindHost: 127.0.0.1` which is already the default configuration. 
It is generally a good idea to reduce the visibility of a service using a firewall or defining some iptable rules.  
  
## Usage
  The default configuration uses http://localhost:9998/expand as base url. You have to add the url parameters `feedUrl` containing the url of the feed you wish to expand. You also need to add the `includes` url parameter which describes the part of the web page which should be extracted and placed in the result feed. There is a simple page at http://localhost:9998/create which helps to create expanded full feeds.
  
  The `includes` url parameter, which selects a part of a html page, describes a navigation path through the
  html elements using a CSS (or jquery) like selector syntax.
  
### Selector overview
  `tagname`: find elements by tag, e.g. `a`
  `ns|tag`: find elements by tag in a namespace, e.g. `fb|name finds <fb:name> elements`
  `#id`: find elements by ID, e.g. `#logo`
  `.class`: find elements by class name, e.g. `.masthead`
  `[attribute]`: elements with attribute, e.g. `[href]`
  `[^attr]`: elements with an attribute name prefix, e.g. `[^data-]` finds elements with HTML5 dataset attributes
  `[attr=value]`: elements with attribute value, e.g. `[width=500]` (also quotable, like [data-name='launch sequence'])
  `[attr^=value], [attr$=value], [attr*=value]`: elements with attributes that start with, end with, or contain the value, e.g. `[href*=/path/]`
  `[attr~=regex]`: elements with attribute values that match the regular expression; e.g. `img[src~=(?i)\.(png|jpe?g)]`
  `*`: all elements, e.g. `*`
  
  See [Selector syntax](https://jsoup.org/apidocs/org/jsoup/select/Selector.html) for more details.
  
## Examples
  You can make use of the feed creation page at http://localhost:9998/create which also does the url encoding for you. The following URL configurations are randomly picked and only used as examples. There exists NO agreement with page proprietor which allows to expand their feeds for commercial or private use. 

  * golem.de feed at rss.golem.de/rss.php?feed=ATOM1.0 can be expanded by including the article tag.
  `http://localhost:9998/expand?feedUrl=http%3A%2F%2Frss.golem.de%2Frss.php%3Ffeed%3DRSS2.0&include=article&limit=10`  
    
  * Java revisited feed at feeds.feedburner.com/Javarevisited can be expanded by including the tag with the class named post.
  `http://127.0.0.1:9998/expand?feedUrl=http%3A%2F%2Ffeeds.feedburner.com%2FJavarevisited&include=.post`
  
  * Pro Linux feed at pro-linux.de/rss/2/3/rss20_aktuell.xml can be expanded by selecting the tag with the id named news.
  `http://127.0.0.1:9998/expand?feedUrl=http%3A%2F%2Fpro-linux.de%2Frss%2F2%2F3%2Frss20_aktuell.xml&include=%23news`
  
  * Android Police at feeds.feedburner.com/AndroidPolice can be expanded by selecting the tag with the id which name begins with post-
  `http://127.0.0.1:9998/expand?feedUrl=http%3A%2F%2Ffeeds.feedburner.com%2FAndroidPolice&include=%5Bid%5E%3Dpost-%5D`
  
  * Asienspiegel asienspiegel.ch/feed/
  `http://127.0.0.1:9998/expand?feedUrl=http%3A%2F%2Fasienspiegel.ch%2Ffeed%2F&include=%23content`
  