package test.xdutils;

import org.xdef.XDValue;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;

public class XdDummyParser {

    public static void initParams(XXElement chkElem) {}
    public static void setErr(XXElement chkElem, XDValue[] params) {}
    public static boolean tab(XXElement chkEl, XDValue[] params) {return true;}
    public static void chkOpt_RC_ifEQ(XXElement chkElem, XDValue[] params) {}
    public static void dateDavka(XXElement chkElem, XDValue[] params) {}
    public static void chk_dec_nonNegative(XXElement chkEl, XDValue[] params) {}
    public static void chk_RC_DatNar_ifEQ(XXElement chkEl, XDValue[] params) {}
    public static void setDefault_ifEx(XXElement chkElem, XDValue[] params) {}
    public static void emptySubjHasAddr(XXElement chkElem, XDValue[] params) {}
    public static String getIdOsoba(XXElement chkElem) { return "1"; }
    public static void protocol(XXElement chkElem, String role, long idXxx) {}
    public static void protocol(XXElement chkElem, String role, String ident) {}
    public static void outputIVR(XXElement chkElem, XDValue[] params) {}
    public static String getKodPartnera() { return "1"; }
    public static void chkEQ_PojistitelFuze(XXElement chkEl, XDValue[] params){}
    public static void chk_Poj_NeexElement(XXElement chkEl, XDValue[] params) {}
    public static void chkOpt_IC_ifEQ(XXElement chkElem, XDValue[] params) {}
    public static void hasElement_if(XXElement chkElem, XDValue[] params) {}
    public static void subjekt_OsobaOrFirma(XXElement chkEl, XDValue[] params){}
    public static String getIdSubjekt(XXElement chkElem) { return "1"; }
    public static void notEmptyMisto(XXElement chkElem, XDValue[] params) {}
    public static void equal(XXElement chkElem, XDValue[] params) {}
    public static void chkOpt_CisloTP_ifEQ(XXElement chkEl, XDValue[] params) {}
    public static String getIdVozidlo(XXElement chkElem) { return "1"; }
    public static boolean kvadrant(XXElement chkElem) { return true; }
    public static void chk_TypMinusPlneni_Platba(XXElement chkEl,
                                                 XDValue[] params) {}
    public static boolean fil0(XXElement chkEl, XDValue[] params) {return true;}
    public static void isEqual(XXNode c, XDValue[] p) {}
    public static void exactlyOneAttr(XXNode c, XDValue[] p){}

}
