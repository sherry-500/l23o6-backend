package org.fffd.l23o6.pojo.vo.train;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.fffd.l23o6.util.strategy.train.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketInfo {
    private String type;
    private Integer count;
    private Integer price;
}