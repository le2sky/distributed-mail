package com.aengdulab.distributedmail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class DistributedSupport {

    private int index;
    private int count;

    public boolean isMine(Long id) {
        return index == ((id % count) + 1);
    }
}
