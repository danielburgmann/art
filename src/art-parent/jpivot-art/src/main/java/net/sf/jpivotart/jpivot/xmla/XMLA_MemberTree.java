/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package net.sf.jpivotart.jpivot.xmla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;
import net.sf.jpivotart.jpivot.olap.mdxparse.Formula;
import net.sf.jpivotart.jpivot.olap.mdxparse.ParsedQuery;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.Level;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapException;
import net.sf.jpivotart.jpivot.olap.navi.MemberTree;
import net.sf.jpivotart.jpivot.util.StringUtil;
import net.sf.jpivotart.jpivot.olap.model.Position;
import net.sf.jpivotart.jpivot.olap.model.Result;
import net.sf.jpivotart.jpivot.olap.query.Quax;
import net.sf.jpivotart.jpivot.olap.model.Axis;

/**
 * Member Tree Implementation vor XMLA
 */
public class XMLA_MemberTree extends ExtensionSupport implements MemberTree {

  static Logger logger = Logger.getLogger(XMLA_MemberTree.class);

  /**
   * Constructor sets ID
   */
  public XMLA_MemberTree() {
    super.setId(MemberTree.ID);
  }

  /**
   * @return the root members of a hierarchy. This is for example
   * the "All" member or the list of measures.
   */
  public Member[] getRootMembers(Hierarchy hier) {
    XMLA_Model model = (XMLA_Model) getModel();
    Level[] levels = hier.getLevels();
    // get root level
    XMLA_Level rootLevel = null;
    for (int i = 0; i < levels.length; i++) {
      XMLA_Level xLev = (XMLA_Level) levels[i];
      if (xLev.getDepth() == 0) {
        rootLevel = xLev;
        break;
      }
    }
    if (rootLevel == null)
      return null; // should not occur

    
    final List<Member> visibleRootMembers = new ArrayList<>();
    final List<XMLA_Member> invisibleRootMembers = new ArrayList<>();
    
    Member[] rootMembers = new Member[0];
    try {
      rootMembers = rootLevel.getMembers();
    } catch (OlapException e) {
      logger.error(null, e);
    }
    // find the calculated members for this hierarchy
    //  show them together with root level members
    ArrayList<XMLA_Member> aCalcMem = new ArrayList<>();
    ParsedQuery pq = ((XMLA_QueryAdapter) model.getQueryAdapter()).getParsedQuery();
    Formula[] formulas = pq.getFormulas();

    for (int i = 0; i < formulas.length; i++) {
      Formula f = formulas[i];
      if (!f.isMember())
        continue;

      String dimUMember = StringUtil.bracketsAround(f.getFirstName());
      String dimUHier = ((XMLA_Hierarchy) hier).getUniqueName();
      if (!(dimUHier.equals(dimUMember)))
        continue;

      String memberName = f.getUniqeName();
      XMLA_Member calcMem = (XMLA_Member) model.lookupMemberByUName(memberName);
      if (calcMem == null) {
          /* Strip brackets from name */
          String[] nameParts = StringUtil.splitUniqueName(memberName);                   
          calcMem = new XMLA_Member(model, memberName, nameParts[1], null, true);
          //calcMem = new XMLA_Member(model, memberName, f.getLastName(), null, true);
      }
      aCalcMem.add(calcMem);
    }
    
    // order members according to occurrence in query result
    //  if there is no result available, do not sort
    // If the result contains invisible members, add them to the list
    Result res = model.currentResult();
    if (res != null) {       
        // locate the appropriate result axis
        // find the Quax for this hier
        XMLA_QueryAdapter adapter = (XMLA_QueryAdapter) model.getQueryAdapter();
        Quax quax = adapter.findQuax(hier.getDimension());
        if (quax != null) {    
            int iDim = quax.dimIdx(hier.getDimension());
            int iAx = quax.getOrdinal();
            if (adapter.isSwapAxes())
              iAx = (iAx + 1) % 2;
            Axis axis = res.getAxes()[iAx];
            List positions = axis.getPositions();
            
            for (Iterator iter = positions.iterator(); iter.hasNext();) {
              Position pos = (Position) iter.next();
              Member[] posMembers = pos.getMembers();
              XMLA_Member mem = (XMLA_Member) posMembers[iDim];
              if (!(getParent(mem) == null))
                continue; // ignore, not root
              if (!visibleRootMembers.contains(mem))
                visibleRootMembers.add(mem);
              
              // Check if the result axis contains invisible members
              boolean containsMember = false;
              for (int i = 0; i < rootMembers.length; i++) {
                  if (rootMembers[i].equals(mem)) {
                      containsMember = true;
                      break;
                  }
              }
              if (!containsMember && !aCalcMem.contains(mem) && !invisibleRootMembers.contains(mem)) {
                  invisibleRootMembers.add(mem);
              }                      
            }
        }
    }
    
    Member[] members = new Member[rootMembers.length + aCalcMem.size() + invisibleRootMembers.size()];
    int k = rootMembers.length;
    for (int i = 0; i < k; i++) {
      members[i] = rootMembers[i];
    }
    for (XMLA_Member calcMem : aCalcMem) {
      members[k++] = calcMem;
    }        
    for (XMLA_Member invisibleMem : invisibleRootMembers) {
        members[k++] = invisibleMem;
    }        
  
    // If there is no query result, do not sort
    if (visibleRootMembers.size() != 0) {
        Arrays.sort(members, new Comparator<Member>() {
          public int compare(Member m1, Member m2) {
            int index1 = visibleRootMembers.indexOf(m1);
            int index2 = visibleRootMembers.indexOf(m2);
            if (index2 == -1)
              return -1; // m2 is higher, unvisible to the end
            if (index1 == -1)
              return 1; // m1 is higher, unvisible to the end
            return index1 - index2;
          }
        });
    }

    return members;
  }

  /**
   * @return true if the member has children
   */
  public boolean hasChildren(Member member) {

    XMLA_Member m = (XMLA_Member) member;
    if (m.isCalculated())
      return false;
    long ccard = m.getChildrenCardinality(); // -1 if not initialized
    if (ccard >= 0)
      return (ccard > 0);
    XMLA_Level xLev = (XMLA_Level) member.getLevel();
    if (xLev == null || xLev.getChildLevel() == null)
      return false;
    return true;
  }

  /**
   * @return the children of the member
   */
  public Member[] getChildren(Member member) {

    XMLA_Level xLev = (XMLA_Level) member.getLevel();

    if (xLev == null || xLev.getChildLevel() == null)
      return null;

    Member[] children = new Member[0];
    try {
      children = ((XMLA_Member) member).getChildren();
    } catch (OlapException e) {
      logger.error("?", e);
      return null;
    }
    return children;
  }

  /**
   * @return the parent of member or null, if this is a root member
   */
  public Member getParent(Member member) {

    XMLA_Level xLev = (XMLA_Level) member.getLevel();

    if (xLev == null || xLev.getDepth() == 0)
      return null; // already top level

    XMLA_Member parent = null;
    try {
      parent = (XMLA_Member) ((XMLA_Member) member).getParent();
    } catch (OlapException e) {
      logger.error("?", e);
      return null;
    }

    return parent;
  }

} // End XMLA_MemberTree

 	  	 
