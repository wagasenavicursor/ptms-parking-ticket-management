package com.ptms.repository;

import com.ptms.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TicketIssueRepository extends JpaRepository<TicketIssue, Long> {
  List<TicketIssue> findAllByOrderByCreatedAtDesc();
  List<TicketIssue> findByStatusOrderByCreatedAtAsc(IssueStatus s);

  @Query("select count(i) from TicketIssue i join i.items item where item.ticket.id = :ticketId and i.status = :status")
  long countByTicketIdAndStatus(@Param("ticketId") Long ticketId, @Param("status") IssueStatus status);
}
