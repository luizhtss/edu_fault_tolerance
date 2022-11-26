package com.labcomu.orcid;

import com.labcomu.faultinjection.annotation.Throw;
import com.labcomu.orcid.resource.Researcher;
import com.labcomu.orcid.resource.mapper.ResearcherMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Service
@Validated
@RequiredArgsConstructor
public class OrcidService {
    /*@Delegate*/ private final OrcidGateway gateway;
    private final ResearcherMapper researcherMapper;

    public boolean isActive() {
        return StringUtils.isNotEmpty(gateway.getStatus());
    }

    @Throw(exception=RuntimeException.class, threshold=0.1)
    public Researcher getResearcher(@NotNull final String orcid) {
        return researcherMapper.map(gateway.getOrcid(orcid));
    }
}
