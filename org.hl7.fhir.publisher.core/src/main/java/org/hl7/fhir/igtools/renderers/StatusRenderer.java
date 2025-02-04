package org.hl7.fhir.igtools.renderers;

import java.util.List;

import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.ContactDetail;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.Utilities;

public class StatusRenderer {

  public static class ResourceStatusInformation {
    String fmm;
    String fmmSupport;
    String owner;
    String ownerLink;
    String status;
    String sstatus;
    String sstatusSupport;
    String normVersion;
    String colorClass;
    public String getFmm() {
      return fmm;
    }
    public void setFmm(String fmm) {
      this.fmm = fmm;
    }
    public String getFmmSupport() {
      return fmmSupport;
    }
    public void setFmmSupport(String fmmSupport) {
      this.fmmSupport = fmmSupport;
    }
    public String getOwner() {
      return owner;
    }
    public void setOwner(String owner) {
      this.owner = owner;
    }
    public String getOwnerLink() {
      return ownerLink;
    }
    public void setOwnerLink(String ownerLink) {
      this.ownerLink = ownerLink;
    }
    public String getStatus() {
      return status;
    }
    public void setStatus(String status) {
      this.status = status;
    }
    public String getSstatus() {
      return sstatus;
    }
    public void setSstatus(String sstatus) {
      this.sstatus = sstatus;
    }
    public String getSstatusSupport() {
      return sstatusSupport;
    }
    public void setSstatusSupport(String sstatusSupport) {
      this.sstatusSupport = sstatusSupport;
    }
    public String getNormVersion() {
      return normVersion;
    }
    public void setNormVersion(String normVersion) {
      this.normVersion = normVersion;
    }
    public String getColorClass() {
      return colorClass;
    }
    public void setColorClass(String colorClass) {
      this.colorClass = colorClass;
    }
    public void processFmm(DomainResource resource) {
      if (ToolingExtensions.hasExtension(resource, ToolingExtensions.EXT_FMM_LEVEL)) {
        setFmm(ToolingExtensions.readStringExtension(resource, ToolingExtensions.EXT_FMM_LEVEL));
        IntegerType fmm = resource.getExtensionByUrl(ToolingExtensions.EXT_FMM_LEVEL).getValueIntegerType();
        if (fmm.hasExtension(ToolingExtensions.EXT_FMM_SUPPORT))
          setFmmSupport(fmm.getExtensionByUrl(ToolingExtensions.EXT_FMM_SUPPORT).getValueStringType().getValue());
        else if (fmm.hasExtension(ToolingExtensions.EXT_FMM_DERIVED)) {
          List<Extension> derivations = fmm.getExtensionsByUrl(ToolingExtensions.EXT_FMM_DERIVED);
          String s = "Inherited from ";
          for (Extension ex: derivations) {
            s += ", " + ex.getValueCanonicalType();
          }
          setFmmSupport(s);
        }
      }
    }
    public void processSstatus(DomainResource resource) {
      if (ToolingExtensions.hasExtension(resource, ToolingExtensions.EXT_STANDARDS_STATUS)) {
        setSstatus(ToolingExtensions.readStringExtension(resource, ToolingExtensions.EXT_STANDARDS_STATUS));
        StringType sstatus = resource.getExtensionByUrl(ToolingExtensions.EXT_STANDARDS_STATUS).getValueStringType();
        if (sstatus.hasExtension(ToolingExtensions.EXT_FMM_SUPPORT))
          setFmmSupport(sstatus.getExtensionByUrl(ToolingExtensions.EXT_FMM_SUPPORT).getValueStringType().getValue());
        else if (sstatus.hasExtension(ToolingExtensions.EXT_FMM_DERIVED)) {
          List<Extension> derivations = sstatus.getExtensionsByUrl(ToolingExtensions.EXT_FMM_DERIVED);
          String s = "Inherited from ";
          for (Extension ex: derivations) {
            s += ", " + ex.getValueCanonicalType();
          }
          setFmmSupport(s);
        }
      }
    }
  }

  public static ResourceStatusInformation analyse(DomainResource resource) {
    ResourceStatusInformation info = new ResourceStatusInformation();
    info.processFmm(resource);
    info.setOwner(readOwner(resource));
    info.setOwnerLink(readOwnerLink(resource));
    info.setStatus(readStatus(resource));
    info.processSstatus(resource);
    info.setNormVersion(readNormativeVersion(resource));
    info.setColorClass(getColor(info));
    return info;
  }

  private static String getColor(ResourceStatusInformation info) {
	  return getColor(info.getStatus(), info.getSstatus(), info.getFmm());
  }
  
  public static String getColor(String status, String sStatus, String fmm) {
    if (sStatus != null)
      switch (sStatus) {
        case "Draft": return "colsd";
        case "Trial-Use": return "0".equals(fmm) ? "colsd" : "colstu"; 
        case "Normative": return "colsn";
        case "Informative": return "colsi";
        case "Deprecated": return "colsdp";
        case "External": return "colse";
      }
    if (fmm != null)
      return "0".equals(fmm) ? "colsd" : "colstu";
    if (status != null)
      switch (status) {
        case "Draft": return "colsd";
        case "Retired": return "colsdp"; 
      }
    return "colsi";
  }


  private static String readStandardsStatus(DomainResource resource) {
    return ToolingExtensions.readStringExtension(resource, ToolingExtensions.EXT_STANDARDS_STATUS);
  }

  
  private static String readNormativeVersion(DomainResource resource) {
    return ToolingExtensions.readStringExtension(resource, ToolingExtensions.EXT_NORMATIVE_VERSION);
  }


  private static String readStatus(DomainResource resource) {
    if (resource instanceof CanonicalResource) {
      return ((CanonicalResource) resource).getStatus().getDisplay();
    }
    return null;
  }


  private static String readOwnerLink(DomainResource resource) {
    if (resource instanceof CanonicalResource) {
      for (ContactDetail cd : ((CanonicalResource) resource).getContact()) {
        for (ContactPoint cp : cd.getTelecom()) {
          if (cp.getSystem() == ContactPointSystem.URL) {
            return cp.getValue();
          }
        }
      }
    }
    return null;
  }


  private static String readOwner(DomainResource resource) {
    if (resource instanceof CanonicalResource) {
      return ((CanonicalResource) resource).hasPublisher() ? ((CanonicalResource) resource).getPublisher() : null;
    }
    return null;
  }


  public static String render(String src, ResourceStatusInformation info) {
    StringBuilder b = new StringBuilder();
    b.append("<table class=\"");
    b.append(info.getColorClass());
    b.append("\"><tr>");
    if (info.getOwnerLink() != null) {
    b.append("<td>Publisher: <a href=\"");
    b.append(info.getOwnerLink());
    b.append("\">");
    b.append(Utilities.escapeXml(info.getOwner()));
    b.append("</a></td><td>");
    } else {
      b.append("<td>Publisher: ");
      b.append(Utilities.escapeXml(info.getOwner()));
      b.append("</td><td>");
    }
    b.append("<a href=\""+src+"/versions.html#maturity\">Status</a>: ");
    b.append(info.getStatus());
    b.append("</td><td>");
    b.append("<a href=\""+src+"/versions.html#maturity\">Maturity Level</a>: ");
    if (info.getFmm() != null) {
      b.append(info.getFmm());
    } else {
      b.append("N/A");      
    }
    b.append("</td><td>");
    b.append("<a href=\""+src+"/versions.html#std-process\">Standards Status</a>: ");
    if (info.getSstatus() != null) {
      b.append(info.getSstatus());
    } else {
      b.append("N/A");      
    }
    b.append("</td></tr></table>\r\n");
    return b.toString();
  }

}
