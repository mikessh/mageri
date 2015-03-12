package com.milaboratory.oncomigec.util;

import org.jdom.Element;

import java.io.Serializable;

public interface ParameterSet extends Serializable {
    public Element toXml();
}
