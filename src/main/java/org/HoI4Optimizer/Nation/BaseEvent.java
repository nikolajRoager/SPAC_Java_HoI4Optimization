package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;

/// Base class for single event
public abstract class BaseEvent {
    static Attribute GoodOutcome= Attribute.GREEN_TEXT();
    static Attribute BadOutcome=Attribute.RED_TEXT();
    static Attribute MiddlingOutcome=Attribute.WHITE_TEXT();
}
