// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/Utils/LoggingUtils.swift
import Foundation
import os

/// Global date formatter for log timestamps. Static to avoid repeated creation.
private let logDateFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS" // Example format
    formatter.locale = Locale(identifier: "en_US_POSIX") // Ensure consistent format
    formatter.timeZone = TimeZone.current // Use local time zone
    return formatter
}()

/// Logs a message with a timestamp using the provided OSLog logger and level.
///
/// - Parameters:
///   - message: The message string to log. Can include interpolation.
///   - level: The OSLogType level (e.g., .info, .debug, .error). Defaults to .debug.
///   - logger: The OSLog logger instance to use.
///   - file: The file where the log originated (automatically captured).
///   - function: The function where the log originated (automatically captured).
///   - line: The line number where the log originated (automatically captured).
func log(
    _ message: @autoclosure () -> String,
    level: OSLogType = .debug,
    logger: Logger,
    file: String = #file,
    function: String = #function,
    line: Int = #line
) {
    let timestamp = logDateFormatter.string(from: Date())
    // Extracting the filename from the full path for brevity
    let fileName = (file as NSString).lastPathComponent
    let logMessage = "\(timestamp) [\(fileName):\(line) \(function)] \(message())"

    // Log the formatted message using the provided logger and level
    // Note: OSLog interpolation privacy rules still apply here.
    // For simplicity, we log the pre-formatted string. Be mindful of sensitive data.
    switch level {
    case .info:
        logger.info("\(logMessage, privacy: .public)")
    case .debug:
        logger.debug("\(logMessage, privacy: .public)")
    case .error:
        logger.error("\(logMessage, privacy: .public)")
    case .fault:
        logger.fault("\(logMessage, privacy: .public)")
    // Note: OSLogType.default maps to logger.log() or logger.info()
    // OSLogType.info maps to logger.info()
    // OSLogType.debug maps to logger.debug()
    // OSLogType.error maps to logger.error()
    // OSLogType.fault maps to logger.fault()
    // There isn't a direct OSLogType for logger.notice(), but we can map .default to it.
    case .default:
        logger.notice("\(logMessage, privacy: .public)") // Map default to notice
    default: // Catch any other levels like .info explicitly if needed, or handle future ones
        // Fallback to notice or info for unhandled cases
        logger.notice("\(logMessage, privacy: .public)")
    }
}
