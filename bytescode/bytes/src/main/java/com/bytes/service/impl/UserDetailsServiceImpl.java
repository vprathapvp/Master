package com.bytes.service.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bytes.model.User;
import com.bytes.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}

		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
				Collections.emptyList()) {
			@Override
			public String getUsername() {
				return user.getEmail();
			}

			public String getFirstName() {
				return user.getFirstName();
			}

			public String getLastName() {
				return user.getLastName();
			}
		};
	}
}