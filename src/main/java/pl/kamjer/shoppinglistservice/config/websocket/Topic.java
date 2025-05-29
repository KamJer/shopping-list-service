package pl.kamjer.shoppinglistservice.config.websocket;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Topic(String topicUrl, String[] parameters) {

    int getParameterCount() {
        return parameters.length;
    }

    boolean isTopicParameterized() {
        return parameters.length > 0;
    }

    String getParameterizedUrl() {
        String[] urlElements = topicUrl.split("/");
        int parameterCount = 0;
        StringBuilder subUrlBuilder = new StringBuilder();
//            beginning of a url element
        for (int i = 0; i < urlElements.length; i++) {

//                if element of url is parameter replace it with passed parameter
            if (urlElements[i].startsWith("{") && urlElements[i].endsWith("}")) {
                subUrlBuilder.append(parameters[parameterCount]);
                parameterCount++;
            } else {
//                    if element is not parameter add it back at its place
                subUrlBuilder.append(urlElements[i]);
            }
            if (i < urlElements.length - 1) {
//                end of a url element with the exception of a last one
                subUrlBuilder.append("/");
            }
        }
        return subUrlBuilder.toString();
    }

    List<String> getUriParameterizedElement() {
        return Stream.of(topicUrl.split("/"))
                .filter(s -> !s.isEmpty())
                .filter(s -> s.startsWith("{"))
                .map(s -> s.substring(1, s.length() - 1))
                .toList();
    }

    Map<String, String> getUriMap() {
        List<String> parameterUris = getUriParameterizedElement();
        List<String> parametersList = List.of(parameters);
        return IntStream.range(0, parameterUris.size())
                .boxed()
                .collect(Collectors.toMap(parameterUris::get, parametersList::get));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Topic topic) {
            return topic.hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return topicUrl.hashCode();
    }
}
