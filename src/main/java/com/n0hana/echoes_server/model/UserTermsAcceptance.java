package com.n0hana.echoes_server.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_terms_acceptance")
public class UserTermsAcceptance {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long id;

  @CreationTimestamp
  private Instant acceptedAt;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
