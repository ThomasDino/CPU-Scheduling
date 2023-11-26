// Pace University
// Fall 2023
// Operating Systems/Architecture
//
// Course: CS 371
// Team Authors: Thomas Dinopoulos
// Collaborators: none
// References: Java, CS371 Lecture slides and provided code
//
// Assignment: 2
// Description: CPUScheduler
package CS371CA2;

import java.util.LinkedList;
import java.util.Random;

public class CPUScheduler {

	public static class Job {
		int jobId;
		int arrivalTime;
		int burst;
		int remainingBurst;
		int startTime;
		int endTime;

		public Job(int jobId, int arrivalTime, int burst) {
			this.jobId = jobId;
			this.arrivalTime = arrivalTime;
			this.burst = burst;
			this.remainingBurst = burst;
			this.startTime = -1;
			this.endTime = -1;
		}

		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("[");
			result.append(jobId);
			result.append("] ");
			result.append("Burst - ");
			result.append(burst);
			result.append(" Arrival - ");
			result.append(arrivalTime);
			result.append(" Start - ");
			result.append(startTime);
			result.append(" Remaining Burst - ");
			result.append(remainingBurst);
			return result.toString();
		}

		public boolean equals(Object o) {
			return ((Job)o).jobId == this.jobId;
		}
	}


	public static class ReadyQueue {		
		private LinkedList<Job> readyQueue;

		public ReadyQueue() {			
			readyQueue = new LinkedList<Job>();
		}

		public void enqueue(Job j) {
			readyQueue.add(j);
		}

		public Job dequeue() {
			if (readyQueue.isEmpty()) {
				return null;
			} else {
				return readyQueue.poll();
			}
		}

		public LinkedList<Job> queue() {
			return readyQueue;
		}
	}


	public static class CPU {
		private ReadyQueue readyQueue;
		private Job running;

		private int lastJobCreated;
		private Random rand;

		public CPU(ReadyQueue readyQueue) {
			this.readyQueue = readyQueue;
			running = null;
			lastJobCreated = 0;
			rand = new Random();
		}

		public void newJob(int sec) {
			// Ensure 20% chance a new job is created.
			if (rand.nextInt(100) < 20) {
				lastJobCreated++;
				Job j = new Job(lastJobCreated, sec, rand.nextInt(10) + 1);
				System.out.println(sec + " === " + j);
				readyQueue.enqueue(j);
			} 
		}

		public Job getRunning() { return running; }

		public void setRunning(int sec, Job j) {
			System.out.println(sec + " <<< " + running);
			running = j;
			if (j == null) {
				System.out.println(sec + " >>> No Job Scheduled");
			} else {
				System.out.println(sec + " >>> " + j);
			}
		}

	}


	public static interface Scheduler {
		public void schedule(int currSec);
	}


	public static class FIFOScheduler implements Scheduler {
		CPU c;
		ReadyQueue r;

		public FIFOScheduler(CPU c, ReadyQueue r) {
			this.c = c;
			this.r = r;
		}


		public void schedule(int currSec) {
			Job runningJob = c.getRunning();

			if (runningJob == null) {
				Job nextJob = r.dequeue();

				if (nextJob != null) {
					nextJob.startTime = currSec;
					c.setRunning(currSec, nextJob);
					int waitingTime = currSec - nextJob.arrivalTime;
					System.out.println("Job " + nextJob.jobId + " started at time " + currSec + ", Waiting Time: " + waitingTime);
				} else {
					c.setRunning(currSec, null);
				}
			} else {
				if (runningJob.remainingBurst <= 1) {
					runningJob.endTime = currSec;
					c.setRunning(currSec, null);
					int turnaroundTime = currSec - runningJob.arrivalTime + 1;
					System.out.println("Job " + runningJob.jobId + " completed at time " + currSec + ", Turnaround Time: " + turnaroundTime);
				} else {
					runningJob.remainingBurst--;
					c.setRunning(currSec, runningJob);
				}
			}
		}


		public static class SRTFScheduler implements Scheduler {
			CPU c;
			ReadyQueue r;

			public SRTFScheduler(CPU c, ReadyQueue r) {
				this.c = c;
				this.r = r;
			}


			public void schedule(int currSec) {
			    Job runningJob = c.getRunning();

			    if (runningJob == null || (r.queue().size() > 0 && r.queue().peek().remainingBurst < runningJob.remainingBurst)) {
			        if (runningJob != null) {
			            r.enqueue(runningJob);
			        }
			        Job shortestRemainingJob = r.dequeue();

			        if (shortestRemainingJob != null) {
			            shortestRemainingJob.startTime = currSec;
			            c.setRunning(currSec, shortestRemainingJob);

			            int waitingTime = currSec - shortestRemainingJob.arrivalTime;
			            System.out.println("Job " + shortestRemainingJob.jobId + " started at time " + currSec + ", Waiting Time: " + waitingTime);
			        } else {
			            c.setRunning(currSec, null);
			        }
			    } else {
			        if (runningJob.remainingBurst <= 1) {
			            runningJob.endTime = currSec;
			            c.setRunning(currSec, null);
			            int turnaroundTime = currSec - runningJob.arrivalTime + 1;
			            System.out.println("Job " + runningJob.jobId + " completed at time " + currSec + ", Turnaround Time: " + turnaroundTime);
			        } else {
			            runningJob.remainingBurst--;
			            c.setRunning(currSec, runningJob);
			        }
			    }
			}



			public static void simulate(int length, CPU c, Scheduler s) {
				for (int sec = 0; sec < length; sec++) {
					c.newJob(sec);
					s.schedule(sec);
				}
			}


			public static void main(String[] args) {
				int MAX_LENGTH = 100;

				System.out.println("==== FIFO ====");
				ReadyQueue r = new ReadyQueue();		
				CPU c = new CPU(r);
				Scheduler f = new FIFOScheduler(c, r);
				simulate(MAX_LENGTH, c, f);

				System.out.println("==== SRTF ====");
				r = new ReadyQueue();
				c = new CPU(r);
				Scheduler s = new SRTFScheduler(c, r);
				simulate(MAX_LENGTH, c, s);
			}
		}
	}
}
