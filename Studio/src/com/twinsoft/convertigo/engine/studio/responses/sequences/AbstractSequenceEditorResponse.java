package com.twinsoft.convertigo.engine.studio.responses.sequences;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.core.Sequence;
import com.twinsoft.convertigo.engine.studio.responses.AbstractResponse;

public abstract class AbstractSequenceEditorResponse extends AbstractResponse {

    protected Sequence sequence;

    public AbstractSequenceEditorResponse(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public Element toXml(Document document, String qname) throws Exception {
        Element response =  super.toXml(document, qname);
        response.setAttribute("project", sequence.getProject().getName());
        response.setAttribute("sequence", sequence.getName());
        // Maybe useless attribute
        response.setAttribute("type_editor", "c8o_sequence_editor");

        return response;
    }
}