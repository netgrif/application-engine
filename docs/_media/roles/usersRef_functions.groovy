Long getDummyUser() {
   return userService.findByEmail("dummy@netgrif.com", true).id
}

void debug() {
   log.debug("Debugging...")
}