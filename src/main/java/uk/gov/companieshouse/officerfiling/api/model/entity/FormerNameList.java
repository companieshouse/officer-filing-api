package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormerNameList extends ArrayList<FormerName> {

    public FormerNameList() {
    }

    public FormerNameList(Collection<? extends FormerName> c) {
        super(clone(c));
    }

    public List<FormerName> getList() {
        return clone(this);
    }

    private static List<FormerName> clone(Collection<? extends FormerName> c) {

        return Optional.ofNullable(c).map(Collection::stream).orElseGet(Stream::empty)
            .map(FormerName::new).collect(Collectors.toList());
    }
}
