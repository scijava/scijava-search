package org.scijava.search.web;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.ws.WebServiceException;

import org.scijava.plugin.Plugin;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;

/**
 * This factory creates actions for opening web search results in a browser
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
@Plugin(type = SearchActionFactory.class)
public class OpenWebSearchResultInBrowserActionFactory implements
                                                       SearchActionFactory
{
  @Override
  public boolean supports(final SearchResult result) {
    return result instanceof WebSearchResult;
  }

  @Override
  public SearchAction create(final SearchResult result) {
    return new DefaultSearchAction("Open in Browser", () -> {
      try {
        Desktop.getDesktop().browse(new URI(result.properties().get("url")));
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (URISyntaxException e1) {
        e1.printStackTrace();
      }
    });
  }
}
