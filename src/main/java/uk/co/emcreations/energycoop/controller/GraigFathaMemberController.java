package uk.co.emcreations.energycoop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.emcreations.energycoop.dto.EnergySaving;
import uk.co.emcreations.energycoop.model.Site;
import uk.co.emcreations.energycoop.security.HasGraigFathaStatsRead;
import uk.co.emcreations.energycoop.security.HasTaxDocumentRead;
import uk.co.emcreations.energycoop.service.GraigFathaMemberService;
import uk.co.emcreations.energycoop.util.PrincipalHelper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/graigFatha/member")
@RequiredArgsConstructor
@Tag(name = "Graig Fatha Membership", description = "Membership endpoints for the Graig Fatha wind farm")
public class GraigFathaMemberController {
    private final GraigFathaMemberService graigFathaMemberService;

    @HasGraigFathaStatsRead
    @GetMapping(name = "Today's Savings", value = "/todaySavings")
    @Operation(summary = "Today's Savings", description = "Returns this user's current savings today")
    public EnergySaving getTodaySavings(final Principal principal) {
        final EnumMap<Site, Double> ownerships = PrincipalHelper.extractOwnershipsFromPrincipal(principal);

        return graigFathaMemberService.getTodaySavings(ownerships.get(Site.GRAIG_FATHA));
    }

    @HasGraigFathaStatsRead
    @GetMapping(name = "Get savings between dates", value = "/savings/{from}/{to}")
    @Operation(summary = "Get savings between dates", description = "Returns this user's savings between dates")
    public Set<EnergySaving> getSavings(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate from,
                                        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate to,
                                        final Principal principal) {
        final EnumMap<Site, Double> ownerships = PrincipalHelper.extractOwnershipsFromPrincipal(principal);
        String userId =  PrincipalHelper.extractUserFromPrincipal(principal);

        return graigFathaMemberService.getSavings(from, to, ownerships.get(Site.GRAIG_FATHA), userId);
    }

    @HasTaxDocumentRead
    @GetMapping(name = "Generate tax document", value = "/tax-document/{from}/{to}")
    @Operation(summary = "Generate a tax document between dates", description = "Returns this user's tax document between dates")
    public ResponseEntity<byte[]> generateTaxDocument(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate from, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate to,
                                                      final Principal principal) throws URISyntaxException, IOException {
        final EnumMap<Site, Double> ownerships = PrincipalHelper.extractOwnershipsFromPrincipal(principal);
        String userId =  PrincipalHelper.extractUserFromPrincipal(principal);

        byte[] content = graigFathaMemberService.generateTaxDocument(from, to, ownerships.get(Site.GRAIG_FATHA), userId);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("TaxDocument.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}