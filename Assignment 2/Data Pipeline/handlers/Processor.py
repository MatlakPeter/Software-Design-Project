import json
import os
import subprocess
from fileinput import filename

from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


def _calculate_encoding_profile(source_bitrate_bps: int):
    return {
        "4k": int(source_bitrate_bps * 1.0),
        "1080p": int(source_bitrate_bps * 0.5),
        "720p": int(source_bitrate_bps * 0.25)
    }


class Processor(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        # both Visual + Audio/Text done here
        print ("=== PROCESSOR ===")
        print("=== VISUALS ===")

        bit_rate = self._get_bitrate()
        self.context.encoding_profile = _calculate_encoding_profile(bit_rate)

        self._create_converted_videos()

        self._create_save_sprite_map()

        return Event.PROCESS

    def _get_bitrate(self):
        filepath_string = str(self.context.file_path)
        command = [
            "ffprobe",
            "-v", "quiet",
            "-select_streams", "v:0",  # Select the first video stream
            "-show_entries", "stream=bit_rate",
            "-print_format", "json",
            filepath_string
        ]
        result = subprocess.run(command, stdout=subprocess.PIPE, text=True)
        data = json.loads(result.stdout)
        try:
            # Extract bitrate and convert from string to integer
            bitrate_bps = int(data['streams'][0]['bit_rate'])
            print(bitrate_bps)
            return bitrate_bps
        except (KeyError, ValueError, TypeError):
            return 5000000

    def _convert_video(self, output_file, resolution_height, codec, bitrate_bps = None):
        filepath_string = str(self.context.file_path)
        # Scale filter ensures the height is set, width scales proportionally (-2 maintains even dimensions)
        scale_filter = f"scale=-2:{resolution_height}"
        command = [
            "ffmpeg",
            "-y", # overwrite output file without asking
            "-i", filepath_string,
            "-vf", scale_filter,
            "-c:v", codec,
            "-preset", "fast"
        ]

        if bitrate_bps is not None:
            bitrate_kbps = int(bitrate_bps / 1000)
            command += ["-b:v", f"{bitrate_kbps}k"]

        command.append(output_file)

        print(f"Transcoding {output_file} with bitrate {bitrate_bps} bps...")
        result = subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if result.returncode != 0:
            raise RuntimeError(result.stdout)

    def _create_converted_videos(self, base_directory="../../../videos/movie_101"):
        video_base_directory = os.path.join(base_directory, "video")
        os.makedirs(video_base_directory, exist_ok=True)

        resolutions = {
            "4k": {"height": 2160, "bitrate": self.context.encoding_profile.get("4k")},
            "1080p": {"height": 1080, "bitrate": self.context.encoding_profile.get("1080p")},
            "720p": {"height": 720, "bitrate": self.context.encoding_profile.get("720p")}
        }
        formats = {
            "h264": {"codec": "libx264", "ext": "mp4"},
            "vp9": {"codec": "libvpx-vp9", "ext": "webm"},
            "hevc": {"codec": "libx265", "ext": "mkv"}
        }

        for fmt_name, fmt_data in formats.items():
            fmt_dir = os.path.join(video_base_directory, fmt_name)
            os.makedirs(fmt_dir, exist_ok=True)

            for res_name, res_data in resolutions.items():
                file_name = f"{res_name}_{fmt_name}.{fmt_data['ext']}"
                output_file_path = os.path.join(fmt_dir, file_name)
                self._convert_video(
                    output_file=output_file_path,
                    resolution_height=res_data["height"],
                    codec=fmt_data["codec"],
                    bitrate_bps=res_data["bitrate"]
                )

                # Save the path of the generated video to Context
                asset_key = f"{res_name}_{fmt_name}"
                self.context.visual_assets[asset_key] = output_file_path

    def _generate_sprite_map(self, output_image):
        filepath_string = str(self.context.file_path)
        # video_filter = "fps=1/10,scale=160:-1,tile=5x5"
        video_filter = "fps=1/1,scale=160:-1,tile=5x5"
        command = [
            "ffmpeg",
            "-y",
            "-i", filepath_string,
            "-frames:v", "1",       # Output a single image file
            "-q:v", "2",
            "-vf", video_filter,
            output_image
        ]
        print("Generating sprite map...")
        result = subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if result.returncode != 0:
            raise RuntimeError(result.stdout)

    def _create_save_sprite_map(self, base_directory="../../../videos/movie_101"):
        image_base_directory = os.path.join(base_directory, "image")
        os.makedirs(image_base_directory, exist_ok=True)
        image_filepath = os.path.join(image_base_directory, "sprite_map.jpg")

        self._generate_sprite_map(image_filepath)

        self.context.visual_assets["sprite_map"] = image_filepath