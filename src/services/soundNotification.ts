import Sound from 'react-native-sound';
import { Platform } from 'react-native';

Sound.setCategory('Playback');

class SoundNotificationService {
  private beepSound: Sound | null = null;
  private isReady: boolean = false;
  private isPlaying: boolean = false;

  private stopTimeout: ReturnType<typeof setTimeout> | null = null;

  private pendingAlertTimeout: ReturnType<typeof setTimeout> | null = null;
  private pendingAlert: { duration: number; alertType: string } | null = null;
  private pendingAlertAttempts: number = 0;
  private readonly maxPendingAlertAttempts: number = 20;

  constructor() {
    this.initializeSound();
  }

  private initializeSound(): void {
    this.isReady = false;

    if (this.beepSound) {
      this.beepSound.release();
      this.beepSound = null;
    }

    try {
      const candidates =
        Platform.OS === 'android'
          ? ['beep_alarm_sound_effect', 'beep_alarm_sound_effect.mp3']
          : ['beep_alarm_sound_effect.mp3', 'beep_alarm_sound_effect'];

      const tryLoad = (index: number) => {
        const soundFile = candidates[index];
        if (!soundFile) {
          this.isReady = false;
          console.error('Failed to load beep sound: no candidates succeeded');
          return;
        }

        const instance = new Sound(soundFile, Sound.MAIN_BUNDLE, loadError => {
          if (loadError) {
            if (this.beepSound === instance) {
              this.beepSound = null;
            }
            instance.release();
            tryLoad(index + 1);
            return;
          }

          this.isReady = true;

          try {
            this.beepSound?.setVolume(1.0);
          } catch (volumeError) {
            if (__DEV__) {
              console.debug('Failed to set beep volume', volumeError);
            }
          }

          if (__DEV__) {
            console.log('Beep sound loaded successfully');
          }
        });

        this.beepSound = instance;
      };

      tryLoad(0);
    } catch (initError) {
      this.isReady = false;
      console.error('Error initializing sound:', initError);
    }
  }

  playAdhanAlert(): void {
    this.playAlert(10000, 'Adhan');
  }

  playIqamahAlert(): void {
    this.playAlert(15000, 'Iqamah');
  }

  getIsReady(): boolean {
    return this.isReady;
  }

  private playAlert(duration: number, alertType: string): void {
    if (!this.beepSound || !this.isReady) {
      this.pendingAlert = { duration, alertType };
      this.pendingAlertAttempts += 1;

      if (this.pendingAlertAttempts > this.maxPendingAlertAttempts) {
        console.error(
          `Giving up on ${alertType} alert; beep sound never became ready`
        );
        this.pendingAlert = null;
        this.pendingAlertAttempts = 0;
        return;
      }

      if (this.pendingAlertTimeout) {
        clearTimeout(this.pendingAlertTimeout);
      }

      if (!this.beepSound) {
        this.initializeSound();
      }
      this.pendingAlertTimeout = setTimeout(() => {
        const pending = this.pendingAlert;
        this.pendingAlertTimeout = null;
        if (!pending) {
          return;
        }

        this.pendingAlert = null;
        this.playAlert(pending.duration, pending.alertType);
      }, 250);

      return;
    }

    this.pendingAlertAttempts = 0;

    this.stopAlert();

    try {
      this.isPlaying = true;
      this.beepSound.setVolume(1.0);
      this.beepSound.setCurrentTime(0);
      this.beepSound.setNumberOfLoops(-1);

      this.beepSound.play(success => {
        if (!success) {
          console.error('Sound playback failed');
          this.initializeSound();
        }
      });

      if (__DEV__) {
        console.log(
          `${alertType} alert playing for ${duration / 1000} seconds`
        );
      }

      this.stopTimeout = setTimeout(() => {
        this.stopAlert();
        if (__DEV__) {
          console.log(
            `${alertType} alert stopped after ${duration / 1000} seconds`
          );
        }
      }, duration);
    } catch (error) {
      console.error('Error playing sound:', error);
      this.isPlaying = false;
    }
  }

  stopAlert(): void {
    if (this.stopTimeout) {
      clearTimeout(this.stopTimeout);
      this.stopTimeout = null;
    }

    if (this.pendingAlertTimeout) {
      clearTimeout(this.pendingAlertTimeout);
      this.pendingAlertTimeout = null;
    }

    this.pendingAlert = null;
    this.pendingAlertAttempts = 0;

    if (this.beepSound) {
      this.beepSound.stop(() => {
        this.beepSound?.setCurrentTime(0);
      });
    }

    this.isPlaying = false;
  }

  getIsPlaying(): boolean {
    return this.isPlaying;
  }

  cleanup(): void {
    this.stopAlert();
    this.isReady = false;

    if (this.beepSound) {
      this.beepSound.release();
      this.beepSound = null;
    }
  }

  reinitialize(): void {
    this.cleanup();
    this.initializeSound();
  }
}

export const soundNotificationService = new SoundNotificationService();
