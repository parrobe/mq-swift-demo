/*
Copyright (c) Rob Parker 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 Contributors:
   Rob Parker - Initial Contribution
*/
package swiftdemoapp;

/**
 * Interface for the Putting and Getting threads enabling simple thread
 * management.
 */
public interface MoneyHandlers extends Runnable {
  /**
   * Signals that this thread should gracefully stop.
   */
  public void signalStop();

  /**
   * Returns whether this thread is still active.
   * 
   * @return True if the thread is active.
   */
  public boolean isActive();
}
