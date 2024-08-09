Long getDummyUser() {
   return userService.findByEmail("dummy@netgrif.com").id
}

void debug() {
   log.debug("Debugging...")
}