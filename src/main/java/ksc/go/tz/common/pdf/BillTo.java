package ksc.go.tz.common.pdf;

import ksc.go.tz.sitesAndAssests.entities.Sites;

import java.util.ArrayList;
import java.util.List;

/**
 * "Bill to" lines describing the site a quotation or invoice is for.
 */
public final class BillTo {

    private BillTo() {
    }

    public static List<String> forSite(Sites site) {
        List<String> lines = new ArrayList<>();
        if (site.getAddressArea() != null) {
            lines.add(site.getAddressArea());
        }
        StringBuilder details = new StringBuilder();
        if (site.getSiteType() != null) {
            details.append(capitalize(site.getSiteType())).append(" site");
        }
        if (site.getAreaSqm() != null) {
            details.append(details.isEmpty() ? "" : " · ").append(site.getAreaSqm().stripTrailingZeros().toPlainString()).append(" m²");
        }
        if (site.getRoomCount() != null) {
            details.append(details.isEmpty() ? "" : " · ").append(site.getRoomCount()).append(site.getRoomCount() == 1 ? " room" : " rooms");
        }
        if (!details.isEmpty()) {
            lines.add(details.toString());
        }
        if (site.getService() != null) {
            lines.add("Service: " + site.getService().getServiceName());
        }
        lines.add("Site ref: " + site.getId());
        return lines;
    }

    private static String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
