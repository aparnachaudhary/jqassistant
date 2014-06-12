package com.buschmais.jqassistant.core.pluginrepository.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.RulesType;
import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.pluginrepository.api.RulePluginRepository;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl implements RulePluginRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulePluginRepositoryImpl.class);

    private List<Source> sources;

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginRepository pluginRepository) throws PluginRepositoryException {
        this.sources = getRuleSources(pluginRepository.getPlugins());
    }

    @Override
    public List<Source> getRuleSources() {
        return sources;
    }

    private List<Source> getRuleSources(List<JqassistantPlugin> plugins) {
        List<Source> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                String directory = rulesType.getDirectory();
                for (ResourcesType resourcesType : rulesType.getResources()) {
                    for (String resource : resourcesType.getResource()) {
                        StringBuffer fullResource = new StringBuffer();
                        if (directory != null) {
                            fullResource.append(directory);
                        }
                        fullResource.append(resource);
                        URL url = RulePluginRepositoryImpl.class.getResource(fullResource.toString());
                        String systemId = null;
                        if (url != null) {
                            try {
                                systemId = url.toURI().toString();
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Adding rulesType from " + url.toString());
                                }
                                InputStream ruleStream = url.openStream();
                                sources.add(new StreamSource(ruleStream, systemId));
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot open rules URL: " + url.toString(), e);
                            } catch (URISyntaxException e) {
                                throw new IllegalStateException("Cannot create URI from url: " + url.toString());
                            }
                        } else {
                            LOGGER.warn("Cannot read rules from resource '{}'", fullResource);
                        }
                    }
                }
            }
        }
        return sources;
    }
}