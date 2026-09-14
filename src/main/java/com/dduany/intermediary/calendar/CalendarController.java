package com.dduany.intermediary.calendar;

import com.dduany.intermediary.config.SecurityConfig;
import com.dduany.intermediary.planitem.PlanItem;
import com.dduany.intermediary.planitem.PlanItemRepository;
import com.dduany.intermediary.planitem.PlanItemStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * An iCalendar feed of dated plan items, so the plan shows up next to real appointments.
 * Calendar apps cannot send bearer tokens, so the feed URL carries its own long-lived token
 * (derived from the JWT secret; rotating the secret rotates the link).
 */
@RestController
@Tag(name = "Calendar", description = "Subscribe to plan items from Google, Apple, or Outlook calendar")
public class CalendarController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final PlanItemRepository planItems;
    private final SecurityConfig.CalendarToken token;

    public CalendarController(PlanItemRepository planItems, SecurityConfig.CalendarToken token) {
        this.planItems = planItems;
        this.token = token;
    }

    @GetMapping("/calendar/link")
    @Operation(summary = "The subscribable feed URL for this account")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, String> link() {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/calendar.ics").queryParam("token", token.value()).toUriString();
        return Map.of("url", url);
    }

    @GetMapping(value = "/calendar.ics", produces = "text/calendar")
    @Operation(summary = "iCalendar feed (token in the query string, no bearer needed)", security = {})
    public ResponseEntity<String> feed(@RequestParam(required = false) String token) {
        if (token == null || !MessageDigest.isEqual(Utf8.encode(token), Utf8.encode(this.token.value()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.TEXT_PLAIN).body("invalid token");
        }
        StringBuilder ics = new StringBuilder();
        line(ics, "BEGIN:VCALENDAR");
        line(ics, "VERSION:2.0");
        line(ics, "PRODID:-//intermediary//plan//EN");
        line(ics, "CALSCALE:GREGORIAN");
        line(ics, "METHOD:PUBLISH");
        line(ics, "X-WR-CALNAME:intermediary plan");
        for (PlanItem item : planItems.findAll()) {
            if (item.getTargetDate() == null || item.getStatus() == PlanItemStatus.CANCELED) continue;
            LocalDate d = item.getTargetDate();
            line(ics, "BEGIN:VEVENT");
            line(ics, "UID:plan-item-" + item.getId() + "@intermediary");
            line(ics, "DTSTAMP:" + stamp(item.getUpdatedAt()));
            line(ics, "DTSTART;VALUE=DATE:" + d.format(DATE));
            line(ics, "DTEND;VALUE=DATE:" + d.plusDays(1).format(DATE));
            String prefix = item.getStatus() == PlanItemStatus.DONE ? "\u2713 " : item.getStatus() == PlanItemStatus.DEFERRED ? "(deferred) " : "";
            line(ics, "SUMMARY:" + escape(prefix + item.getTitle()));
            String desc = item.getIntent() + " · " + item.getStatus() + (item.getNotes() == null ? "" : "\n" + item.getNotes());
            line(ics, "DESCRIPTION:" + escape(desc));
            line(ics, "STATUS:" + (item.getStatus() == PlanItemStatus.DONE ? "CONFIRMED" : "TENTATIVE"));
            line(ics, "END:VEVENT");
        }
        line(ics, "END:VCALENDAR");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .header("Cache-Control", "no-cache")
                .body(ics.toString());
    }

    private static String stamp(Instant t) {
        return (t == null ? Instant.now() : t).atOffset(ZoneOffset.UTC).format(STAMP);
    }

    /** RFC 5545 text escaping. */
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\r\n", "\n").replace("\n", "\\n");
    }

    /** Lines are CRLF-terminated and folded at 75 octets. */
    private static void line(StringBuilder out, String text) {
        int max = 75;
        if (text.length() <= max) {
            out.append(text).append("\r\n");
            return;
        }
        out.append(text, 0, max).append("\r\n");
        for (int i = max; i < text.length(); i += max - 1) {
            out.append(' ').append(text, i, Math.min(text.length(), i + max - 1)).append("\r\n");
        }
    }
}
