package com.n0hana.echoes_server.service.notifier;

import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.TwoFactorDto;

@Service
public class LoggerNotifier implements TwoFactorNotifier {
  @Override
  public void send(TwoFactorDto dto) {
    System.out.println(dto.email() + " : " + dto.code());
  }
}
