/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filters;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author massi
 */
public class AllowDenyFilter implements IFileFilter{
    protected List<IFileFilter> allowRules=new ArrayList<IFileFilter>();
    protected List<IFileFilter> denyRules=new ArrayList<IFileFilter>();
    
    public void addAllowRule(IFileFilter rule)
    {
        allowRules.add(rule);
    }
    
    public void addDenyRule(IFileFilter rule)
    {
        denyRules.add(rule);
    }
    
    @Override
    public boolean isFiltered(AbstractFilesystem file) {
        for(IFileFilter rule:denyRules)
        {
            if(rule.isFiltered(file)) {
                return true;
            }
        }
        
        for(IFileFilter rule:allowRules)
        {
            if(rule.isFiltered(file)) {
                return false;
            }
        }
        return true;
    } 
}
