public class userprofile {
    public class UserProfile {
        private String username;
        private String fullName;
        private String progress;
    
        // Constructor
        public UserProfile(String username, String fullName, String progress) {
            this.username = username;
            this.fullName = fullName;
            this.progress = progress;
        }
    
        // Getters and setters
        public String getUsername() {
            return username;
        }
    
        public void setUsername(String username) {
            this.username = username;
        }
    
        public String getFullName() {
            return fullName;
        }
    
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    
        public String getProgress() {
            return progress;
        }
    
        public void setProgress(String progress) {
            this.progress = progress;
        }
    }    
}
